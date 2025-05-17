/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.systick;

import java.util.HashMap;
import java.util.Map;

import peripheralsimulation.utils.RegisterMap;
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
	 * 					Register offsets (constants) 					  *
	 * ------------------------------------------------------------------ *
	 */
	public static final int CSR_OFFSET = 0x010;
	public static final int RVR_OFFSET = 0x014;
	public static final int CVR_OFFSET = 0x018;
	public static final int CALIB_OFFSET = 0x01C;

	private static final Map<String, Integer> NAME2OFFSET = new HashMap<>();
	static {
		NAME2OFFSET.put("SYST_CSR", CSR_OFFSET);
		NAME2OFFSET.put("SYST_RVR", RVR_OFFSET);
		NAME2OFFSET.put("SYST_CVR", CVR_OFFSET);
		NAME2OFFSET.put("SYST_CALIB", CALIB_OFFSET);
	}

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

	/** RegisterMap object to access register values. */
	private RegisterMap registerMap;

	/**
	 * Constructor for SysTickTimerConfig.
	 *
	 * @param registerMap RegisterMap object to access register values
	 */
	public SysTickTimerConfig(RegisterMap registerMap) {
		this.registerMap = registerMap;
		this.SYST_CSR = registerMap.getRegisterValue(CSR_OFFSET);
		this.SYST_RVR = registerMap.getRegisterValue(RVR_OFFSET) & RegisterUtils.BIT_MASK;
		this.SYST_CVR = registerMap.getRegisterValue(CVR_OFFSET) & RegisterUtils.BIT_MASK;
		this.SYST_CALIB = registerMap.getRegisterValue(CALIB_OFFSET);
	}

	// -------------- GET/SET for SYST_CSR bits --------------
	public int getCSR() {
		return SYST_CSR;
	}

	public void setCSR(int val) {
		registerMap.setRegisterValue(CSR_OFFSET, val);
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
		registerMap.setRegisterValue(CSR_OFFSET, SYST_CSR);
	}

	public void setTickInt(boolean tickInt) {
		if (tickInt) {
			SYST_CSR |= (1 << 1);
		} else {
			SYST_CSR &= ~(1 << 1);
		}
		registerMap.setRegisterValue(CSR_OFFSET, SYST_CSR);
	}

	public void setUseCpuClock(boolean use) {
		if (use) {
			SYST_CSR |= (1 << 2);
		} else {
			SYST_CSR &= ~(1 << 2);
		}
		registerMap.setRegisterValue(CSR_OFFSET, SYST_CSR);
	}

	// -------------- GET/SET for SYST_RVR --------------
	public int getRVR() {
		return SYST_RVR & RegisterUtils.BIT_MASK;
	}

	public void setRVR(int value) {
		SYST_RVR = value & RegisterUtils.BIT_MASK;
		registerMap.setRegisterValue(RVR_OFFSET, SYST_RVR & RegisterUtils.BIT_MASK);
	}

	// -------------- GET/SET for SYST_CVR --------------
	public int getCVR() {
		return SYST_CVR & RegisterUtils.BIT_MASK;
	}

	public void setCVR(int value) {
		// writing any value => sets CVR=0, clears COUNTFLAG
		SYST_CVR = 0;
		registerMap.setRegisterValue(CVR_OFFSET, SYST_CVR);
		// If code sets a separate bit for countFlag in SYST_CSR, clear it here
		// e.g. SYST_CSR &= ~(1<<16)
	}

	// --------- GET for SYST_CALIB (read-only) ---------
	public int getCALIB() {
		return SYST_CALIB;
	}

	/**
	 * Returns the offset of a register by its name.
	 *
	 * @param name The name of the register.
	 * @return The offset of the register, or -1 if not found.
	 */
	public static int getRegisterOffset(String name) {
		return NAME2OFFSET.getOrDefault(name, -1);
	}

}
