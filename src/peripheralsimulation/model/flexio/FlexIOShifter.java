/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.flexio;

/**
 * Class representing a FlexIO shifter.
 * 
 * This class provides methods to configure and manage the shifter's settings,
 * including its mode, pin selection, and buffer.
 * 
 * @author Veronika Lenková
 */
public class FlexIOShifter {

	/** Shifter disabled mode */
	private static final int SMOD_DISABLED = 0b000;
	/** Shifter receive mode */
	private static final int SMOD_RECEIVE = 0b001;
	/** Shifter transmit mode */
	private static final int SMOD_TRANSMIT = 0b010;

	/** Index of the shifter */
	private int index;
	/** FlexIO configuration object */
	private FlexIOConfig config;
	/** FlexIO timer object asociated with this shifter */
	private FlexIOTimer timer;
	/** Count of bits shifted (0-31) */
	private int bitCnt;
	/** Current output level (0 - low, 1 - high) */
	private boolean pinLevel;
	/** Boolean flag indicating if the start bit has been processed */
	private boolean startDone;
	/** Boolean flag indicating if the buffer contains data to be sent */
	private boolean bufValid;

	/*****************************************************************/
	/* SHIFTCFG */
	/*****************************************************************/

	/** Mode of the shifter (disabled, RX, TX, Match store, etc.) */
	private int smod;
	/** Pin polarity, 0 - Pin is active high, 1 - active low */
	private int pinPol;
	/** Pin selection, 0 - Pin 0, 1 - Pin 1, etc. */
	private int pinSel;
	/** Pin configuration (output disabled, open drain, etc.) */
	private int pinCfg;
	/** Timer polarity, 0 - Shift on posedge of Shift clock, 1 - on negative edge */
	private int timPol;
	/** Timer selection, 0 - Timer 0, 1 - Timer 1, etc. */
	private int timSel;

	/*****************************************************************/
	/* SHIFTCTL */
	/*****************************************************************/

	/** Start bit, allows automatic start bit insertion */
	private int sstart;
	/** Stop bit, allows automatic stop bit insertion */
	private int sstop;
	/** Input source, 0 - Pin, 1 - Shifter N+1 output */
	private int insrc;

	/** SHIFBUF - Shifter buffer */
	private int buffer;

	/**
	 * Constructor for FlexIOShifter.
	 * 
	 * @param config The FlexIO configuration object.
	 * @param index  The index of the shifter.
	 */
	public FlexIOShifter(FlexIOConfig config, int index) {
		this.index = index;
		this.config = config;
		setControlRegister(config.getShiftCtl(index));
		setConfigRegister(config.getShiftCfg(index));
		setBuffer(config.getShiftBuf(index));
	}

	/**
	 * Sets the shifter control register.
	 * 
	 * @param shifterControlRegister The control register value.
	 */
	public void setControlRegister(int shifterControlRegister) {
		this.smod = shifterControlRegister & 0x07;
		this.pinPol = (shifterControlRegister >> 7) & 1;
		this.pinSel = (shifterControlRegister >> 8) & 7;
		this.pinCfg = (shifterControlRegister >> 16) & 3;
		this.timPol = (shifterControlRegister >> 23) & 1;
		this.timSel = (shifterControlRegister >> 24) & 3;
		if (timSel < config.getTimers().length) {
			this.timer = config.getTimers()[timSel];
		}
	}

	/**
	 * Sets the shifter configuration register.
	 * 
	 * @param shifterConfigRegister The configuration register value.
	 */
	public void setConfigRegister(int shifterConfigRegister) {
		this.sstart = (shifterConfigRegister >> 0) & 3;
		this.sstop = (shifterConfigRegister >> 4) & 3;
		this.insrc = (shifterConfigRegister >> 8) & 1;
	}

	/**
	 * This method sets the initial values for the shifter's state.
	 */
	public void reset() {
		bitCnt = 0;
		startDone = false;
		bufValid = false;
		pinLevel = (pinPol == 1); // default HIGH (= 1) → po XOR môže byť 0
	}

	/**
	 * Simulate the shifter's operation based on the shifter mode.
	 *
	 * @param clockEdge The clock edge (rising or falling) that triggers the shift
	 *                  operation.
	 */
	public void shift(boolean clockEdge) {
		if (timer == null || smod == SMOD_DISABLED || !clockEdge)
			return;

		// * vyber, či je to hrana, na ktorej máme shiftovať */
		boolean posEdge = timer.isClockLevelHigh();
		if ((timPol == 0 && !posEdge) || (timPol == 1 && posEdge))
			return;

		/*
		 * počet dátových bitov v slove z CMP[15:8]: reloadHigh = ((CMP[15:8] + 1) / 2)
		 * → bits = reloadHigh
		 */
		int wordBits = timer.getHighReload();

		// pre demo – SHIFT na každom log. 1 (OUT high):
		switch (smod) {

		/* ============ TRANSMIT ============ */
		case SMOD_TRANSMIT -> {

			/* (1) start-bit */
			if (sstart == 0b10 && !startDone) {
				pinLevel = (pinPol == 1); // start-bit value 0 (RM)
				startDone = true;
				return;
			}

			/* (2) data bits */
			boolean bit = (buffer & 1) != 0;
			pinLevel = (bit ^ (pinPol == 1));
			buffer >>>= 1;

			/* (3) stop-bit po dosiahnutí bit-count */
			if (++bitCnt == wordBits) {
				if (sstop == 0b11) { // stop = ‘1’
					pinLevel = !(pinPol == 1);
				}
				// prázdny SHIFTBUF? → underrun
				if (!bufValid)
					config.setShiftErr(1 << index);
				/* automatický load novej hodnoty do shiftra */
				buffer = config.getShiftBuf(index);
				bitCnt = 0;
				bufValid = false;
				startDone = false;
				/* SSF flag – prázdny buffer → požiadavka na DMA/IRQ */
				config.setShiftStat(1 << index);
			}
		}

		/* ============ RECEIVE ============ */
		case SMOD_RECEIVE -> {

			/* start-bit kontrola */
			if (!startDone && sstart == 0b10) {
				boolean start = timer.isClockLevelHigh() ^ (pinPol == 1);
				if (start)
					return; // ešte sme v start-bite
				startDone = true; // hrana do high = stred 1. bitu
			}

			/* sample input */
			boolean inBit;
			if (insrc == 0) { // z pinu
				inBit = timer.isClockLevelHigh() ^ (pinPol == 1);
			} else { // z výstupu (N+1) shiftra
				FlexIOShifter next = config.getShifters()[(index + 1) % config.getShiftersCount()];
				inBit = next.pinLevel;
			}
			buffer >>>= 1;
			if (inBit)
				buffer |= 0x8000_0000;

			if (++bitCnt == wordBits) {
				/* stop-bit check */
				if (sstop == 0b11) {
					boolean stop = timer.isClockLevelHigh() ^ (pinPol == 1);
					if (!stop)
						config.setShiftErr(1 << index); // chyba!
				}
				if (bufValid) {
					config.setShiftErr(1 << index);
				}
				config.setShiftBuf(index, buffer); // presun do SHIFTBUF
				bufValid = true;
				config.setShiftStat(1 << index); // SSF=1
				bitCnt = 0;
				startDone = false;
			}
		}

		default -> {
			/* ostatné SMOD neimplementované */ }
		}
	}

	/**
	 * Returns the current pin level.
	 *
	 * @return true if the pin level is high, false otherwise.
	 */
	public boolean isPinLevelHigh() {
		return pinLevel;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getSmod() {
		return smod;
	}

	public void setSmod(int mode) {
		this.smod = mode;
	}

	public int getPinSel() {
		return pinSel;
	}

	public void setPinSel(int pinSel) {
		this.pinSel = pinSel;
	}

	public int getPinCfg() {
		return pinCfg;
	}

	public void setPinCfg(int pinCfg) {
		this.pinCfg = pinCfg;
	}

	public int getPinPol() {
		return pinPol;
	}

	public void setPinPol(int pinPol) {
		this.pinPol = pinPol;
	}

	public int getTimSel() {
		return timSel;
	}

	public void setTimSel(int timSel) {
		this.timSel = timSel;
	}

	public int getTimPol() {
		return timPol;
	}

	public void setTimPol(int timPol) {
		this.timPol = timPol;
	}

	public int getSstart() {
		return sstart;
	}

	public void setSstart(int sstart) {
		this.sstart = sstart;
	}

	public int getSstop() {
		return sstop;
	}

	public void setSstop(int sstop) {
		this.sstop = sstop;
	}

	public int getInsrc() {
		return insrc;
	}

	public void setInsrc(int insrc) {
		this.insrc = insrc;
	}

	public int getBuffer() {
		return buffer;
	}

	public void setBuffer(int buffer) {
		this.buffer = buffer;
	}

}
