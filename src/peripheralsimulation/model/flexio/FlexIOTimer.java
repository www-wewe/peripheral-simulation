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

	/** Index of the timer */
	public int index;

	/*****************************************************************/
	/* 							TIMCTL		    					 */
	/*****************************************************************/

	/** Mode of the timer (disabled, Dual 8-bit counters baud/bit, PWM, etc.) */
	public int timod;
	/** Pin polarity, 0 - Pin is active high, 1 - active low */
	public int pinPol;
	/** Pin selection, 0 - Pin 0, 1 - Pin 1, etc. */
	public int pinSel;
	/** Pin configuration (output disabled, open drain, etc.) */
	public int pinCfg;
	/** Trigger Source, 0 - external trigger, 1 - internal trigger */
	public int trgSrc;
	/** Trigger polarity, 0 - Trigger is active high, 1 - active low */
	public int trgPol;
	/**
	 * Trigger selection (pin 2n input, shifter n status flag, pin 2n+1 input, timer
	 * n trigger output)
	 */
	public int trgSel;

	/*****************************************************************/
	/* 							TIMCFG		    					 */
	/*****************************************************************/

	/** Timer output, configures the initial state of the timer output */
	public int timOut;
	/**
	 * Timer decrement, configures the source of the timer decrement and source of
	 * the shift clock
	 */
	public int timDec;
	/** Timer reset - condition that resets the timer */
	public int timRst;
	/** Timer disable - condition that disables the timer */
	public int timDis;
	/** Timer enable - condition that enables the timer */
	public int timEna;
	/** Timer stop bit */
	public int tStop;
	/** Timer start bit */
	public int tStart;

	/* TIMCMP - Timer compare value */
	public int cmp;

	/**
	 * Constructor for FlexIOTimer.
	 * 
	 * @param config The FlexIO configuration object.
	 * @param index  The index of the timer.
	 */
	public FlexIOTimer(FlexIOConfig config, int index) {
		this.index = index;
		setControlRegister(config.getTimCtl(index));
		setConfigRegister(config.getTimCfg(index));
		setCmp(config.getTimCmp(index));
	}

	public void setControlRegister(int timerControlRegister) {
		this.timod = timerControlRegister & 3;
		this.pinPol = (timerControlRegister >> 7) & 1;
		this.pinSel = (timerControlRegister >> 8) & 7;
		this.pinCfg = (timerControlRegister >> 16) & 3;
		this.trgSrc = (timerControlRegister >> 22) & 1;
		this.trgPol = (timerControlRegister >> 23) & 1;
		this.trgSel = (timerControlRegister >> 24) & 0xF;
	}

	public void setConfigRegister(int timerConfigRegister) {
		this.timOut = (timerConfigRegister >> 24) & 3;
		this.timDec = (timerConfigRegister >> 20) & 3;
		this.timRst = (timerConfigRegister >> 16) & 7;
		this.timDis = (timerConfigRegister >> 12) & 7;
		this.timEna = (timerConfigRegister >> 8) & 7;
		this.tStop = (timerConfigRegister >> 4) & 3;
		this.tStart = (timerConfigRegister >> 1) & 1;
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
	}

}
