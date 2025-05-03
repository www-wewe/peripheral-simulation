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

	/* TIMCTL */
	public int timod;
	public int pinSel;
	public int pinCfg;
	public int pinPol;
	public int trgSel;
	public int trgPol;
	public int trgSrc;

	/* TIMCFG */
	public int timOut;
	public int timDec;
	public int timRst;
	public int timDis;
	public int timEna;
	public int tStart;
	public int tStop;

	/* TIMCMP & bežiaci counter */
	public int cmp;
	public int counter;

	/**
	 * Constructor for FlexIOTimer.
	 * 
	 * @param config The FlexIO configuration object.
	 * @param index  The index of the timer.
	 */
	public FlexIOTimer(FlexIOConfig config, int index) {
		this.index = index;
		// Initialize timer with default values
		int timerControlRegister = config.getTimCtl(index);
		int timerConfigRegister = config.getTimCfg(index);
		int timerCompareRegister = config.getTimCmp(index);

		this.timod = timerControlRegister & 3;
		this.pinPol = (timerControlRegister >> 7) & 1;
		this.pinSel = (timerControlRegister >> 8) & 7;
		this.pinCfg = (timerControlRegister >> 16) & 3;
		this.trgSrc = (timerControlRegister >> 22) & 1;
		this.trgPol = (timerControlRegister >> 23) & 1;
		this.trgSel = (timerControlRegister >> 24) & 7;

		this.timOut = (timerConfigRegister >> 24) & 3;
		this.timDec = (timerConfigRegister >> 20) & 3;
		this.timRst = (timerConfigRegister >> 16) & 7;
		this.timDis = (timerConfigRegister >> 12) & 7;
		this.timEna = (timerConfigRegister >> 8) & 7;
		this.tStop = (timerConfigRegister >> 4) & 3;
		this.tStart = (timerConfigRegister >> 1) & 1;

		this.cmp = timerCompareRegister & 0xFFFF;
		this.counter = this.cmp;
	}

	public void reload() {
		counter = cmp;
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
		this.cmp = cmp;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

}
