/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.flexio;

/**
 * Class representing a FlexIO timer.
 * 
 * This class provides methods to configure and manage the timer's settings,
 * including its mode, pin selection, and compare value.
 * 
 * @author Veronika Lenková
 */
public class FlexIOTimer {

	/** Timer disabled mode */
	private static final int TIMOD_DISABLED = 0b00;
	/** Dual 8-bit counters baud/bit mode */
	private static final int TIMOD_BAUDBIT = 0b01;
	/** Dual 8-bit counters PWM mode */
	private static final int TIMOD_PWM = 0b10;
	/** Single 16-bit timer mode */
	private static final int TIMOD_16BIT = 0b11;

	/** Index of the timer */
	private int index;
	/** FlexIO configuration object */
	private FlexIOConfig config;
	/** Counter value (low / high) */
	private int counterLow, counterHigh;
	/** Actual output level */
	private boolean outLevel;
	/** Reload value (low / high) from CMP register */
	private int lowReload, highReload;
	/** Boolean flag indicating if the timer is running */
	private boolean running;

	/*****************************************************************/
	/* TIMCTL */
	/*****************************************************************/

	/** Mode of the timer (disabled, Dual 8-bit counters baud/bit, PWM, etc.) */
	private int timod;
	/** Pin polarity, 0 - Pin is active high, 1 - active low */
	private int pinPol;
	/** Pin selection, 0 - Pin 0, 1 - Pin 1, etc. */
	private int pinSel;
	/** Pin configuration (output disabled, open drain, etc.) */
	private int pinCfg;
	/** Trigger Source, 0 - external trigger, 1 - internal trigger */
	private int trgSrc;
	/** Trigger polarity, 0 - Trigger is active high, 1 - active low */
	private int trgPol;
	/**
	 * Trigger selection (pin 2n input, shifter n status flag, pin 2n+1 input, timer
	 * n trigger output)
	 */
	private int trgSel;

	/*****************************************************************/
	/* TIMCFG */
	/*****************************************************************/

	/** Timer output, configures the initial state of the timer output */
	private int timOut;
	/**
	 * Timer decrement, configures the source of the timer decrement and source of
	 * the shift clock
	 */
	private int timDec;
	/** Timer reset - condition that resets the timer */
	private int timRst;
	/** Timer disable - condition that disables the timer */
	private int timDis;
	/** Timer enable - condition that enables the timer */
	private int timEna;
	/** Timer stop bit */
	private int tStop;
	/** Timer start bit */
	private int tStart;

	/** TIMCMP - Timer compare value */
	private int cmp;

	/**
	 * Constructor for FlexIOTimer.
	 * 
	 * @param config The FlexIO configuration object.
	 * @param index  The index of the timer.
	 */
	public FlexIOTimer(FlexIOConfig config, int index) {
		this.index = index;
		this.config = config;
		setControlRegister(config.getTimCtl(index));
		setConfigRegister(config.getTimCfg(index));
		setCmp(config.getTimCmp(index));
	}

	/**
	 * Sets the timer control register.
	 * 
	 * @param timerControlRegister The control register value.
	 */
	public void setControlRegister(int timerControlRegister) {
		this.timod = timerControlRegister & 3;
		this.pinPol = (timerControlRegister >> 7) & 1;
		this.pinSel = (timerControlRegister >> 8) & 7;
		this.pinCfg = (timerControlRegister >> 16) & 3;
		this.trgSrc = (timerControlRegister >> 22) & 1;
		this.trgPol = (timerControlRegister >> 23) & 1;
		this.trgSel = (timerControlRegister >> 24) & 0xF;
	}

	/**
	 * Sets the timer configuration register.
	 * 
	 * @param timerConfigRegister The configuration register value.
	 */
	public void setConfigRegister(int timerConfigRegister) {
		this.timOut = (timerConfigRegister >> 24) & 3;
		this.timDec = (timerConfigRegister >> 20) & 3;
		this.timRst = (timerConfigRegister >> 16) & 7;
		this.timDis = (timerConfigRegister >> 12) & 7;
		this.timEna = (timerConfigRegister >> 8) & 7;
		this.tStop = (timerConfigRegister >> 4) & 3;
		this.tStart = (timerConfigRegister >> 1) & 1;
	}

	/**
	 * This method sets the initial values for the timer's counter and output level
	 * based on the configuration.
	 */
	public void reset() {
		counterLow = lowReload;
		counterHigh = highReload;
		outLevel = (timOut & 1) == 0; // RM: 00/10 -> logic 1
		running = (timEna == 0) || (timEna == 1 && trgSrc == 1);
	}

	/**
	 * This method simulates the timer's behavior, decrementing the counter and
	 * toggling the output level based on the timer mode.
	 *
	 * @return true if the output level changed, false otherwise.
	 */
	public boolean tick() {

		if (!running) {
			if (timEna == 0 || (timEna == 1 && trgSrc == 1)) // was stop bit
				running = true;
			// (ďalšie možnosti – podľa trig/pin – zjednodušené)
		}

		/* timer povolený len ak TIMENA==0 (Always) a TIMDIS==0 */
		if (!running || timDis != 0)
			return false;

		boolean edge = false;

		switch (timod) {

		/*---------------- DUAL-8-bit PWM ----------------------------*/
		case TIMOD_PWM -> {
			if (outLevel) {
				if (--counterLow == 0) {
					counterLow = lowReload;
					outLevel = false;
					edge = true;
				}
			} else {
				if (--counterHigh == 0) {
					counterHigh = highReload;
					outLevel = true;
					edge = true;
				}
			}
		}

		/*---------------- DUAL-8-bit BAUD/BIT -----------------------*/
		case TIMOD_BAUDBIT -> {
			if (--counterLow == 0) {
				counterLow = lowReload;
				outLevel = !outLevel;
				edge = true;
				if (--counterHigh == 0) {
					counterHigh = highReload;
					config.setTimStat(1 << index);
				}
			}
		}

		default -> {
			/* ostatné TIMOD neimplementované */ }
		}

		/*------------------ Stop bit -------------------------*/
		if (edge && tStop != 0 && counterHigh == highReload) {
			// simulácia: v cykle za edge prerušíme beh na 1 bit-čas
			running = false;
		}

		return edge;
	}

	public boolean isClockLevelHigh() {
		boolean level = outLevel;
		if (pinPol == 1) {
			level = !level;
		}
		return level;
	}

	public int getHighReload() {
		return highReload;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getTimod() {
		return timod;
	}

	public void setTimod(int timod) {
		this.timod = timod;
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

	public int getTrgSel() {
		return trgSel;
	}

	public void setTrgSel(int trgSel) {
		this.trgSel = trgSel;
	}

	public int getTrgPol() {
		return trgPol;
	}

	public void setTrgPol(int trgPol) {
		this.trgPol = trgPol;
	}

	public int getTrgSrc() {
		return trgSrc;
	}

	public void setTrgSrc(int trgSrc) {
		this.trgSrc = trgSrc;
	}

	public int getTimOut() {
		return timOut;
	}

	public void setTimOut(int timOut) {
		this.timOut = timOut;
	}

	public int getTimDec() {
		return timDec;
	}

	public void setTimDec(int timDec) {
		this.timDec = timDec;
	}

	public int getTimRst() {
		return timRst;
	}

	public void setTimRst(int timRst) {
		this.timRst = timRst;
	}

	public int getTimDis() {
		return timDis;
	}

	public void setTimDis(int timDis) {
		this.timDis = timDis;
	}

	public int getTimEna() {
		return timEna;
	}

	public void setTimEna(int timEna) {
		this.timEna = timEna;
	}

	public int gettStart() {
		return tStart;
	}

	public void settStart(int tStart) {
		this.tStart = tStart;
	}

	public int gettStop() {
		return tStop;
	}

	public void settStop(int tStop) {
		this.tStop = tStop;
	}

	public int getCmp() {
		return cmp;
	}

	public void setCmp(int cmp) {
		this.cmp = cmp & 0xFFFF;
		switch (timod) {
		case TIMOD_PWM -> { // low / high perióda
			lowReload = (cmp & 0xFF) + 1;
			highReload = ((cmp >>> 8) & 0xFF) + 1;
		}
		case TIMOD_BAUDBIT -> { // baud-divider + bits*2-1
			lowReload = ((cmp & 0xFF) + 1) * 2;
			highReload = ((cmp >>> 8) & 0xFF) + 1; // bits = (N+1)/2
		}
		case TIMOD_16BIT -> { // celý 16-bit counter
			lowReload = (cmp + 1) * 2; // divider
			highReload = 1; // nepoužíva sa
		}
		default -> {
			lowReload = highReload = 1;
		}
		}
	}

}
