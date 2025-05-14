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
	private int shifterMode;
	/** Pin polarity, 0 - Pin is active high, 1 - active low */
	private int shifterPinPolarity;
	/** Pin selection, 0 - Pin 0, 1 - Pin 1, etc. */
	private int shifterPinSelect;
	/** Pin configuration (output disabled, open drain, etc.) */
	private int shifterPinConfiguration;
	/** Timer polarity, 0 - Shift on posedge of Shift clock, 1 - on negative edge */
	private int timerPolarity;
	/** Timer selection, 0 - Timer 0, 1 - Timer 1, etc. */
	private int timerSelect;

	/*****************************************************************/
	/* SHIFTCTL */
	/*****************************************************************/

	/** Start bit, allows automatic start bit insertion */
	private int shifterStartBit;
	/** Stop bit, allows automatic stop bit insertion */
	private int shifterStopBit;
	/** Input source, 0 - Pin, 1 - Shifter N+1 output */
	private int inputSource;

	/** SHIFBUF - Shifter buffer */
	private int shiftBuffer;

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
		this.shifterMode = shifterControlRegister & 0x07;
		this.shifterPinPolarity = (shifterControlRegister >> 7) & 1;
		this.shifterPinSelect = (shifterControlRegister >> 8) & 7;
		this.shifterPinConfiguration = (shifterControlRegister >> 16) & 3;
		this.timerPolarity = (shifterControlRegister >> 23) & 1;
		this.timerSelect = (shifterControlRegister >> 24) & 3;
		if (timerSelect < config.getTimers().length) {
			this.timer = config.getTimers()[timerSelect];
		}
	}

	/**
	 * Sets the shifter configuration register.
	 * 
	 * @param shifterConfigRegister The configuration register value.
	 */
	public void setConfigRegister(int shifterConfigRegister) {
		this.shifterStartBit = (shifterConfigRegister >> 0) & 3;
		this.shifterStopBit = (shifterConfigRegister >> 4) & 3;
		this.inputSource = (shifterConfigRegister >> 8) & 1;
	}

	/**
	 * This method sets the initial values for the shifter's state.
	 */
	public void reset() {
		bitCnt = 0;
		startDone = false;
		shiftBuffer = config.getShiftBuf(index);
		if (shiftBuffer == 0) {
			bufValid = false; // buffer is empty;
		} else {
			bufValid = true;
		}
		pinLevel = !(shifterPinPolarity == 1); // default HIGH (= 1) → po XOR môže byť 0
	}

	/**
	 * Simulate the shifter's operation based on the shifter mode.
	 *
	 * @param edge The clock edge (rising or falling) that triggers the shift
	 *             operation.
	 */
	public void shift(Edge edge) {
		if (timer == null || shifterMode == SMOD_DISABLED || edge == Edge.NONE)
			return;

		// * vyber, či je to hrana, na ktorej máme shiftovať */
		if ((timerPolarity == 0 && edge != Edge.POSEDGE) || (timerPolarity == 1 && edge != Edge.NEGEDGE))
			return;

		/*
		 * počet dátových bitov v slove z CMP[15:8]: reloadHigh = ((CMP[15:8] + 1) / 2)
		 * → bits = reloadHigh
		 */
		int wordBits = timer.getHighReload() / 2;

		// pre demo – SHIFT na každom log. 1 (OUT high):
		// Only sstart=10 a sstop=11 is implemented
		switch (shifterMode) {

		/* ============ TRANSMIT ============ */
		case SMOD_TRANSMIT -> {

			/* (1) start-bit */
			if (shifterStartBit == 0b10 && !startDone) {
				pinLevel = (shifterPinPolarity == 1); // start-bit value 0 (RM)
				startDone = true;
				return;
			}

			/* (2) data bits */
			boolean bit = (shiftBuffer & 1) != 0;
			pinLevel = (bit ^ (shifterPinPolarity == 1));
			shiftBuffer >>>= 1;

			/* (3) stop-bit po dosiahnutí bit-count */
			if (++bitCnt == wordBits) {
				if (shifterStopBit == 0b11) { // stop = ‘1’
					pinLevel = !(shifterPinPolarity == 1);
				}
				// prázdny SHIFTBUF? → underrun
				if (!bufValid)
					config.setShiftErr(1 << index);
				/* automatický load novej hodnoty do shiftra */
				shiftBuffer = config.getShiftBuf(index);
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
			if (!startDone && shifterStartBit == 0b10) {
				boolean start = timer.isClockLevelHigh() ^ (shifterPinPolarity == 1);
				if (start)
					return; // ešte sme v start-bite
				startDone = true; // hrana do high = stred 1. bitu
			}

			/* sample input */
			boolean inBit;
			if (inputSource == 0) { // z pinu
				inBit = timer.isClockLevelHigh() ^ (shifterPinPolarity == 1);
			} else { // z výstupu (N+1) shiftra
				FlexIOShifter next = config.getShifters()[(index + 1) % config.getShiftersCount()];
				inBit = next.pinLevel;
			}
			shiftBuffer >>>= 1;
			if (inBit)
				shiftBuffer |= 0x8000_0000;

			if (++bitCnt == wordBits) {
				/* stop-bit check */
				if (shifterStopBit == 0b11) {
					boolean stop = timer.isClockLevelHigh() ^ (shifterPinPolarity == 1);
					if (!stop)
						config.setShiftErr(1 << index); // chyba!
				}
				if (bufValid) {
					config.setShiftErr(1 << index);
				}
				config.setShiftBuf(index, shiftBuffer); // presun do SHIFTBUF
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

	public int getTimerSelect() {
		return timerSelect;
	}

	public void setBuffer(int buffer) {
		this.shiftBuffer = buffer;
	}

}
