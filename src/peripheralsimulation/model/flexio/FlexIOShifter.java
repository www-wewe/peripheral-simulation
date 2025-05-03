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

	/* SHIFTCTL register */
	private int smod; // 0 disabled, 1 RX, 2 TX, 4 Match store…
	private int pinSel;
	private int pinCfg;
	private int pinPol;
	private int timSel;
	private int timPol;

	/* SHIFTCFG register */
	private int sstart; // start-bit config
	private int sstop; // stop-bit config
	private int insrc;

	/* SHIFTBUF (TX/RX) + stavový flag */
	private int buffer;
	private boolean flag = false;

	/**
	 * Constructor for FlexIOShifter.
	 * 
	 * @param config The FlexIO configuration object.
	 * @param index  The index of the shifter.
	 */
	public FlexIOShifter(FlexIOConfig config, int index) {
		this.index = index;

		int shifterControlRegister = config.getShiftCtl(index);
		int shifterConfigRegister = config.getShiftCfg(index);

		this.smod = shifterControlRegister & 0x07;
		this.pinPol = (shifterControlRegister >> 7) & 1;
		this.pinSel = (shifterControlRegister >> 8) & 7;
		this.pinCfg = (shifterControlRegister >> 16) & 3;
		this.timPol = (shifterControlRegister >> 23) & 1;
		this.timSel = (shifterControlRegister >> 24) & 3;

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

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

}
