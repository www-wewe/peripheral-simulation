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

	/** Index of the shifter */
	private int index;

	/*****************************************************************/
    /* 							SHIFTCFG		    				 */
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
	/*							 SHIFTCTL							 */
	/*****************************************************************/

	/** Start bit, allows automatic start bit insertion */
	private int sstart;
	/** Stop bit, allows automatic stop bit insertion */
	private int sstop;
	/** Input source, 0 - Pin, 1 - Shifter N+1 output */
	private int insrc;

	/* SHIFBUF - Shifter buffer */
	private int buffer;

	/**
	 * Constructor for FlexIOShifter.
	 * 
	 * @param config The FlexIO configuration object.
	 * @param index  The index of the shifter.
	 */
	public FlexIOShifter(FlexIOConfig config, int index) {
		this.index = index;
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
