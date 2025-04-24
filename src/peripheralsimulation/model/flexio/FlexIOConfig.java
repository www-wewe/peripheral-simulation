/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.flexio;

import peripheralsimulation.utils.RegisterUtils;

// TODO asi je tam veľa modov, sprav koľko sa bude dať, nebude vadiť keď nebude všetko, 
// ale vedieť to odvôvodniť ako ľahko to doimplementovať

//MCXN947

/**
 * Holds the raw FlexIO register values and offers typed helpers (get/set of
 * individual bit-fields). Nothing in here “runs” – the
 * {@link peripheralsimulation.model.FlexIOModel} will read / write through this
 * config object.
 *
 *  ┌──────────────────────── Base address (example NXP K64) ────────────────┐
 *  │ 0x4005_A000  FLEXIO_CTRL                                               │
 *  │ 0x4005_A004  FLEXIO_PIN (optional – not modelled below)                │
 *  │ 0x4005_A080  FLEXIO_SHIFTCTL0                                          │
 *  │ 0x4005_A084  FLEXIO_SHIFTCFG0                                          │
 *  │ 0x4005_A0C0  FLEXIO_TIMCTL0                                            │
 *  │ 0x4005_A0C4  FLEXIO_TIMCFG0                                            │
 *  │ 0x4005_A0C8  FLEXIO_TIMCMP0                                            │
 *  └────────────────────────────────────────────────────────────────────────┘
 * 
 * @author Veronika Lenková
 */
public class FlexIOConfig {

	/*
	 * ------------------------------------------------------------------ * 
	 * 					Register addresses (constants) 					  *
	 * ------------------------------------------------------------------ *
	 */
	public static final int FLEXIO_BASE = 0x4005A000; // example
	public static final int CTRL_ADDR = FLEXIO_BASE + 0x00;

	// --- First shifter (index 0) ---------------------------------------
	public static final int SHIFTCTL0_ADDR = FLEXIO_BASE + 0x080;
	public static final int SHIFTCFG0_ADDR = FLEXIO_BASE + 0x084;

	// --- First timer (index 0) -----------------------------------------
	public static final int TIMCTL0_ADDR = FLEXIO_BASE + 0x0C0;
	public static final int TIMCFG0_ADDR = FLEXIO_BASE + 0x0C4;
	public static final int TIMCMP0_ADDR = FLEXIO_BASE + 0x0C8;

	/*
	 * ------------------------------------------------------------------ *
	 * 					Clock frequencies 		    					  *
	 * ------------------------------------------------------------------ *
	 */
	private double mainClk; // e.g. 48e6
	private double externalClk; // e.g. 12e6

	/*
	 * ------------------------------------------------------------------ * 
	 * 				Backing fields holding raw 32‑bit register values 		 	      *
	 * ------------------------------------------------------------------ *
	 */
	private int REG_CTRL;
	private int REG_SHIFTCTL0;
	private int REG_SHIFTCFG0;
	private int REG_TIMCTL0;
	private int REG_TIMCFG0;
	private int REG_TIMCMP0;

	/*
	 * ------------------------------------------------------------------ *
	 * 				Constructor – supply optional reset values 			  *
	 * ------------------------------------------------------------------ *
	 */
	public FlexIOConfig() {
		// Reset values cropped from NXP RM (all zeros is fine for most sims)
		REG_CTRL = 0x00000000;
		REG_SHIFTCTL0 = 0x00000000;
		REG_SHIFTCFG0 = 0x00000000;
		REG_TIMCTL0 = 0x00000000;
		REG_TIMCFG0 = 0x00000000;
		REG_TIMCMP0 = 0x00000000;
		mainClk = 48e6;
		externalClk = 12e6;
	}

	/* ================================================================== */
	/* 						CTRL register helpers 						  */
	/* ================================================================== */

	/** Whole-register getters & setters */
	public int getCTRL() {
		return REG_CTRL;
	}

	public void setCTRL(int v) {
		REG_CTRL = v;
	}

	/** Bit-field helpers (only the most common ones) */
	public boolean isEnable() {
		return (REG_CTRL & 0x01) != 0;
	}

	public void setEnable(boolean e) {
		if (e)
			REG_CTRL |= 0x01;
		else
			REG_CTRL &= ~0x01;
	}

	/** DOZEN: Doze-mode enable (bit 1) */
	public boolean isDozeEnable() {
		return (REG_CTRL & 0x02) != 0;
	}

	public void setDozeEnable(boolean d) {
		if (d)
			REG_CTRL |= 0x02;
		else
			REG_CTRL &= ~0x02;
	}

	/** DBGE: Debug-enable (bit 2) */
	public boolean isDebugEnable() {
		return (REG_CTRL & 0x04) != 0;
	}

	public void setDebugEnable(boolean d) {
		if (d)
			REG_CTRL |= 0x04;
		else
			REG_CTRL &= ~0x04;
	}

	/* ================================================================== */
	/* 					SHIFTCTL0 register helpers 						  */
	/* ================================================================== */

	public int getSHIFTCTL0() {
		return REG_SHIFTCTL0;
	}

	public void setSHIFTCTL0(int v) {
		REG_SHIFTCTL0 = v;
	}

	/** TIMSEL (bits 24‑26) – which timer drives this shifter. */
	public int getShift0TimerSel() {
		return (REG_SHIFTCTL0 >> 24) & 0x07;
	}

	public void setShift0TimerSel(int t) {
		REG_SHIFTCTL0 &= ~(0x07 << 24);
		REG_SHIFTCTL0 |= (t & 0x07) << 24;
	}

	/** PINCFG (bits 4‑5) – output pin config */
	public int getShift0PinCfg() {
		return (REG_SHIFTCTL0 >> 4) & 0x03;
	}

	public void setShift0PinCfg(int pc) {
		REG_SHIFTCTL0 &= ~(0x03 << 4);
		REG_SHIFTCTL0 |= (pc & 0x03) << 4;
	}

	/* ================================================================== */
	/* 					SHIFTCFG0 register helpers						  */
	/* ================================================================== */

	public int getSHIFTCFG0() {
		return REG_SHIFTCFG0;
	}

	public void setSHIFTCFG0(int v) {
		REG_SHIFTCFG0 = v;
	}

	/** PWIDTH (bits 0‑4) – number of bits in each word minus one. */
	public int getShift0PWidth() {
		return REG_SHIFTCFG0 & 0x1F;
	}

	public void setShift0PWidth(int w) {
		REG_SHIFTCFG0 &= ~0x1F;
		REG_SHIFTCFG0 |= (w & 0x1F);
	}

	/* ================================================================== */
	/* 					Timer-0 helpers (similar idea) 					  */
	/* ================================================================== */

	public int getTIMCTL0() {
		return REG_TIMCTL0;
	}

	public void setTIMCTL0(int v) {
		REG_TIMCTL0 = v;
	}

	public int getTIMCFG0() {
		return REG_TIMCFG0;
	}

	public void setTIMCFG0(int v) {
		REG_TIMCFG0 = v;
	}

	public int getTIMCMP0() {
		return REG_TIMCMP0 & RegisterUtils.BIT_MASK;
	}

	public void setTIMCMP0(int v) {
		REG_TIMCMP0 = v & RegisterUtils.BIT_MASK;
	}

	/* ================================================================== */
	/* 	 Address-based access helpers (used by SimulationEngine events)   */
	/* ================================================================== */

	/** Reads any FlexIO register by absolute address */
	public Integer readByAddress(int addr) {
		switch (addr) {
		case CTRL_ADDR:
			return REG_CTRL;
		case SHIFTCTL0_ADDR:
			return REG_SHIFTCTL0;
		case SHIFTCFG0_ADDR:
			return REG_SHIFTCFG0;
		case TIMCTL0_ADDR:
			return REG_TIMCTL0;
		case TIMCFG0_ADDR:
			return REG_TIMCFG0;
		case TIMCMP0_ADDR:
			return REG_TIMCMP0;
		default:
			return null;
		}
	}

	/**
	 * Writes a value to a FlexIO register by absolute address
	 */
	public void writeByAddress(int addr, int value) {
		switch (addr) {
		case CTRL_ADDR:
			setCTRL(value);
			break;
		case SHIFTCTL0_ADDR:
			setSHIFTCTL0(value);
			break;
		case SHIFTCFG0_ADDR:
			setSHIFTCFG0(value);
			break;
		case TIMCTL0_ADDR:
			setTIMCTL0(value);
			break;
		case TIMCFG0_ADDR:
			setTIMCFG0(value);
			break;
		case TIMCMP0_ADDR:
			setTIMCMP0(value);
			break;
		default:
			throw new IllegalArgumentException(String.format("FlexIO: illegal write to 0x%08X", addr));
		}
	}

	/* ================================================================== */
	/* 					Clock info (read-only helpers) 					  */
	/* ================================================================== */

	public double getMainClk() {
		return mainClk;
	}

	public double getExternalClk() {
		return externalClk;
	}
}
