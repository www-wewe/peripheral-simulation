package peripheralsimulation.model.systick;

import peripheralsimulation.utils.RegisterUtils;

public class SysTickTimerConfig {

	// Register addresses for clarity
	public static final int SYST_CSR_ADDR = 0xE000E010;
	public static final int SYST_RVR_ADDR = 0xE000E014;
	public static final int SYST_CVR_ADDR = 0xE000E018;
	public static final int SYST_CALIB_ADDR = 0xE000E01C;

	// CPU frequency or external frequencies
	private double mainClk; // e.g. 48e6
	private double externalClk; // e.g. 12e6

	private int SYST_CSR; // Control and Status Register
	private int SYST_RVR; // Reload Value Register
	private int SYST_CVR; // Current Value Register
	private int SYST_CALIB; // Calibration Value Register (optional, read-only)

	public SysTickTimerConfig(int systCSR, int systRVR, int systCVR, int systCALIB, double mainClk,
			double externalClk) {
		this.SYST_CSR = systCSR;
		this.SYST_RVR = systRVR & RegisterUtils.BIT_MASK;
		this.SYST_CVR = systCVR & RegisterUtils.BIT_MASK;
		this.SYST_CALIB = systCALIB; // read-only

		this.mainClk = mainClk;
		this.externalClk = externalClk;
	}

	// -------------- GET/SET for SYST_CSR bits --------------
	public int getCSR() {
		return SYST_CSR;
	}

	public void setCSR(int val) {
		SYST_CSR = val;
	}

	public boolean isEnabled() {
		return ((SYST_CSR >> 0) & 1) == 1;
	}

	public boolean isTickInt() {
		return ((SYST_CSR >> 1) & 1) == 1;
	}

	public boolean isUseCpuClock() {
		return ((SYST_CSR >> 2) & 1) == 1;
	}

	public void setEnabled(boolean enable) {
		if (enable) {
			SYST_CSR |= (1 << 0);
		} else {
			SYST_CSR &= ~(1 << 0);
		}
	}

	public void setTickInt(boolean tickInt) {
		if (tickInt) {
			SYST_CSR |= (1 << 1);
		} else {
			SYST_CSR &= ~(1 << 1);
		}
	}

	public void setUseCpuClock(boolean use) {
		if (use) {
			SYST_CSR |= (1 << 2);
		} else {
			SYST_CSR &= ~(1 << 2);
		}
	}

	// -------------- GET/SET for SYST_RVR --------------
	public int getRVR() {
		return SYST_RVR & RegisterUtils.BIT_MASK;
	}

	public void setRVR(int value) {
		SYST_RVR = value & RegisterUtils.BIT_MASK;
	}

	// -------------- GET/SET for SYST_CVR --------------
	public int getCVR() {
		return SYST_CVR & RegisterUtils.BIT_MASK;
	}

	public void setCVR(int value) {
		// writing any value => sets CVR=0, clears COUNTFLAG
		SYST_CVR = 0;
		// If your code sets a separate bit for countFlag in SYST_CSR, you might clear
		// it here
		// e.g. SYST_CSR &= ~(1<<16)
	}

	// -------------- GET/SET for SYST_CALIB --------------
	public int getCALIB() {
		return SYST_CALIB;
	}
	// read-only? If so, no setCALIB

	// -------------- Frequencies --------------
	public double getMainClk() {
		return mainClk;
	}

	public double getExternalClk() {
		return externalClk;
	}
}
