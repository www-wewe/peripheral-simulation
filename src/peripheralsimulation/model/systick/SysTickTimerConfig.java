/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.systick;

import peripheralsimulation.utils.RegisterUtils;

/**
 * SysTickTimerConfig class represents the configuration of the SysTick timer.
 * It includes methods to get and set various registers and flags, as well as
 * manage the timer's behavior.
 *
 * @author Veronika Lenková
 */
public class SysTickTimerConfig {

	/*
	 * ------------------------------------------------------------------ *
	 * 					Register addresses (constants) 					  *
	 * ------------------------------------------------------------------ *
	 */
	public static final int SYST_BASE = 0xE000E000;
	public static final int SYST_CSR_ADDR = SYST_BASE + 0x010;
	public static final int SYST_RVR_ADDR = SYST_BASE + 0x014;
	public static final int SYST_CVR_ADDR = SYST_BASE + 0x018;
	public static final int SYST_CALIB_ADDR = SYST_BASE + 0x01C;

	/*
	 * ------------------------------------------------------------------ *
	 * 						Clock frequencies 		    				  *
	 * ------------------------------------------------------------------ *
	 */
	private double mainClk; // e.g. 48e6
	private double externalClk; // e.g. 12e6

	/*
	 * ------------------------------------------------------------------ *
	 * 							Register values	    					  *
	 * ------------------------------------------------------------------ *
	 */
	/** Control and Status Register */
	private int SYST_CSR;
	/** Reload Value Register */
	private int SYST_RVR;
	/** Current Value Register */
	private int SYST_CVR;
	/** Calibration Value Register (optional, read-only) */
	private int SYST_CALIB;

	/**
	 * Constructor for SysTickTimerConfig.
	 * 
	 * @param systCSR     Control and Status Register value
	 * @param systRVR     Reload Value Register value
	 * @param systCVR     Current Value Register value
	 * @param systCALIB   Calibration Value Register value (optional, read-only)
	 * @param mainClk     Main clock frequency
	 * @param externalClk External clock frequency
	 */
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
		// If code sets a separate bit for countFlag in SYST_CSR, clear it here
		// e.g. SYST_CSR &= ~(1<<16)
	}

	// --------- GET for SYST_CALIB (read-only) ---------
	public int getCALIB() {
		return SYST_CALIB;
	}

	// -------------- Frequencies --------------
	public double getMainClk() {
		return mainClk;
	}

	public double getExternalClk() {
		return externalClk;
	}
}
