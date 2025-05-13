/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.flexio;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import peripheralsimulation.utils.RegisterMap;

/**
 * Holds the raw FlexIO register values and offers typed helpers (get/set of
 * individual bit-fields). Nothing in here “runs” – the
 * {@link peripheralsimulation.model.FlexIOModel} will read / write through this
 * config object.
 * 
 * @author Veronika Lenková
 */
public class FlexIOConfig {

	/*
	 * ------------------------------------------------------------------ * 
	 * 					Register offsets (constants) 					  *
	 * ------------------------------------------------------------------ *
	 */
	public static final int VERID_OFFSET = 0x000;
	public static final int PARAM_OFFSET = 0x004;
	public static final int CTRL_OFFSET = 0x008;

	public static final int SHIFTSTAT_OFFSET = 0x010;
	public static final int SHIFTERR_OFFSET = 0x014;
	public static final int TIMSTAT_OFFSET = 0x018;

	public static final int SHIFTSIEN_OFFSET = 0x020;
	public static final int SHIFTEIEN_OFFSET = 0x024;
	public static final int TIMIEN_OFFSET = 0x028;
	public static final int SHIFTSDEN_OFFSET = 0x030;

	// --- First shifter (index 0) ---------------------------------------
	public static final int SHIFTCTL0_OFFSET = 0x080;
	public static final int SHIFTCFG0_OFFSET = 0x100;
	public static final int SHIFTBUF0_OFFSET = 0x200;
	public static final int SHIFTBUFBIS0_OFFSET = 0x280;
	public static final int SHIFTBUFBYS0_OFFSET = 0x300;
	public static final int SHIFTBUFBBS0_OFFSET = 0x380;
	public static final int SHIFTER_STRIDE = 0x004;

	// --- First timer (index 0) -----------------------------------------
	public static final int TIMCTL0_OFFSET = 0x400;
	public static final int TIMCFG0_OFFSET = 0x480;
	public static final int TIMCMP0_OFFSET = 0x500;
	public static final int TIMER_STRIDE = 0x004;

	private static final Map<String, Integer> NAME2OFFSET = new HashMap<>();

	static {
		// Pre-fill the NAME2OFFSET map with all registers
		NAME2OFFSET.put("FLEXIO_VERID", VERID_OFFSET);
		NAME2OFFSET.put("FLEXIO_PARAM", PARAM_OFFSET);
		NAME2OFFSET.put("FLEXIO_CTRL", CTRL_OFFSET);
		NAME2OFFSET.put("FLEXIO_SHIFTSTAT", SHIFTSTAT_OFFSET);
		NAME2OFFSET.put("FLEXIO_SHIFTERR", SHIFTERR_OFFSET);
		NAME2OFFSET.put("FLEXIO_TIMSTAT", TIMSTAT_OFFSET);
		NAME2OFFSET.put("FLEXIO_SHIFTSIEN", SHIFTSIEN_OFFSET);
		NAME2OFFSET.put("FLEXIO_SHIFTEIEN", SHIFTEIEN_OFFSET);
		NAME2OFFSET.put("FLEXIO_TIMIEN", TIMIEN_OFFSET);
		NAME2OFFSET.put("FLEXIO_SHIFTSDEN", SHIFTSDEN_OFFSET);

		for (int i = 0; i < 4; i++) {
			NAME2OFFSET.put("FLEXIO_SHIFTCFG" + i, SHIFTCFG0_OFFSET + i * SHIFTER_STRIDE);
			NAME2OFFSET.put("FLEXIO_SHIFTCTL" + i, SHIFTCTL0_OFFSET + i * SHIFTER_STRIDE);
			NAME2OFFSET.put("FLEXIO_SHIFTBUF" + i, SHIFTBUF0_OFFSET + i * SHIFTER_STRIDE);
			NAME2OFFSET.put("FLEXIO_SHIFTBUFBIS" + i, SHIFTBUFBIS0_OFFSET + i * SHIFTER_STRIDE);
			NAME2OFFSET.put("FLEXIO_SHIFTBUFBYS" + i, SHIFTBUFBYS0_OFFSET + i * SHIFTER_STRIDE);
			NAME2OFFSET.put("FLEXIO_SHIFTBUFBBS" + i, SHIFTBUFBBS0_OFFSET + i * SHIFTER_STRIDE);
			NAME2OFFSET.put("FLEXIO_TIMCFG" + i, TIMCFG0_OFFSET + i * TIMER_STRIDE);
			NAME2OFFSET.put("FLEXIO_TIMCMP" + i, TIMCMP0_OFFSET + i * TIMER_STRIDE);
			NAME2OFFSET.put("FLEXIO_TIMCTL" + i, TIMCTL0_OFFSET + i * TIMER_STRIDE);
		}
	}

	/*
	 * ------------------------------------------------------------------ * 
	 * 				Register values 		 							  *
	 * ------------------------------------------------------------------ *
	 */
	/** FlexIO parameter register - counts of shifters, timers, pins, triggers */
	private int PARAM;
	/** FlexIO control register */
	private int CTRL;
	/** Shifter status flag, 0 - Status flag is clear, 1 - Status flag is set */
	private int SHIFTSTAT;
	/** Shifter error flag, 0 - Error flag is clear, 1 - Error flag is set */
	private int SHIFTERR;
	/** Timer status flag, 0 - Status flag is clear, 1 - Status flag is set */
	private int TIMSTAT;
    /** Shifter status interrupt enable register, 0 - Interrupt disabled, 1 - Interrupt enabled */	
	private int SHIFTSIEN;
	/** Shifter error interrupt enable register, 0 - Interrupt disabled, 1 - Interrupt enabled */
	private int SHIFTEIEN;
	/** Shifter status DMA enable register, 0 - DMA request is disabled, 1 - DMA request enabled */
	private int SHIFTSDEN;
	/** Timer status interrupt enable register, 0 - Interrupt disabled, 1 - Interrupt enabled */
	private int TIMIEN;

	/* Shifter specific registers */
	private int[] SHIFTCTL;
	private int[] SHIFTCFG;
	private int[] SHIFTBUF;
	private int[] SHIFTBUFBIS;
	private int[] SHIFTBUFBYS;
	private int[] SHIFTBUFBBS;

	/* Timer specific registers */
	private int[] TIMCTL;
	private int[] TIMCFG;
	private int[] TIMCMP;

	/*
	 * ------------------------------------------------------------------ * 
	 * 					Number of shifters / timers 					  *
	 * ------------------------------------------------------------------ *
	 */
	private int shiftersCount;
	private int timersCount;
//	private int pinCount;

	/** Jednotlivé konfigurácie: index == číslo periférie. */
	private FlexIOShifter[] shifters;
	private FlexIOTimer[] timers;

	/** Globálne riadiace bity z CTRL. */
	private boolean flexEn, dbgE, dozen; // swRst, fastAcc

	// Chip modes = Run, Stop/Wait, Low Leakage Stop, Debug

	/** RegisterMap object to access register values. */
	private RegisterMap registerMap;

	/**
	 * Constructor for FlexIOConfig.
	 *
	 * @param registerMap The RegisterMap object containing the register offsets and
	 *                    register values.
	 */
	public FlexIOConfig(RegisterMap registerMap) {
		this.registerMap = registerMap;

		PARAM = registerMap.getRegisterValue(PARAM_OFFSET);
		if (PARAM != 0) {
			shiftersCount = PARAM & 0xFF;
			timersCount = (PARAM >> 8) & 0xFF;
//			pinCount = (PARAM >> 16) & 0xFF;
		} else {
			shiftersCount = countBlocks(SHIFTCTL0_OFFSET, SHIFTER_STRIDE);
			timersCount = countBlocks(TIMCTL0_OFFSET, TIMER_STRIDE);
		}

		SHIFTCTL = new int[shiftersCount];
		SHIFTCFG = new int[shiftersCount];
		SHIFTBUF = new int[shiftersCount];
		SHIFTBUFBIS = new int[shiftersCount];
		SHIFTBUFBYS = new int[shiftersCount];
		SHIFTBUFBBS = new int[shiftersCount];
		TIMCTL = new int[timersCount];
		TIMCFG = new int[timersCount];
		TIMCMP = new int[timersCount];

		setCTRL(registerMap.getRegisterValue(CTRL_OFFSET));
		SHIFTSTAT = registerMap.getRegisterValue(SHIFTSTAT_OFFSET);
		SHIFTERR = registerMap.getRegisterValue(SHIFTERR_OFFSET);
		TIMSTAT = registerMap.getRegisterValue(TIMSTAT_OFFSET);
		SHIFTSIEN = registerMap.getRegisterValue(SHIFTSIEN_OFFSET);
		SHIFTEIEN = registerMap.getRegisterValue(SHIFTEIEN_OFFSET);
		TIMIEN = registerMap.getRegisterValue(TIMIEN_OFFSET);
		SHIFTSDEN = registerMap.getRegisterValue(SHIFTSDEN_OFFSET);

		for (int i = 0; i < shiftersCount; i++) {
			SHIFTCTL[i] = registerMap.getRegisterValue(SHIFTCTL0_OFFSET + i * SHIFTER_STRIDE);
			SHIFTCFG[i] = registerMap.getRegisterValue(SHIFTCFG0_OFFSET + i * SHIFTER_STRIDE);
			SHIFTBUF[i] = registerMap.getRegisterValue(SHIFTBUF0_OFFSET + i * SHIFTER_STRIDE);
			SHIFTBUFBIS[i] = registerMap.getRegisterValue(SHIFTBUFBIS0_OFFSET + i * SHIFTER_STRIDE);
			SHIFTBUFBYS[i] = registerMap.getRegisterValue(SHIFTBUFBYS0_OFFSET + i * SHIFTER_STRIDE);
			SHIFTBUFBBS[i] = registerMap.getRegisterValue(SHIFTBUFBBS0_OFFSET + i * SHIFTER_STRIDE);
		}

		for (int i = 0; i < timersCount; i++) {
			TIMCTL[i] = registerMap.getRegisterValue(TIMCTL0_OFFSET + i * TIMER_STRIDE);
			TIMCFG[i] = registerMap.getRegisterValue(TIMCFG0_OFFSET + i * TIMER_STRIDE);
			TIMCMP[i] = registerMap.getRegisterValue(TIMCMP0_OFFSET + i * TIMER_STRIDE);
		}

		shifters = new FlexIOShifter[shiftersCount];
		timers = new FlexIOTimer[timersCount];
		Arrays.setAll(timers, i -> new FlexIOTimer(this, i));
		Arrays.setAll(shifters, i -> new FlexIOShifter(this, i));
	}

	/* ================================================================== */
	/* 						CTRL register helpers 						  */
	/* ================================================================== */

	/** Whole-register getters & setters */
	public int getCTRL() {
		return CTRL;
	}

	public void setCTRL(int value) {
		registerMap.setRegisterValue(CTRL_OFFSET, value);
		CTRL = value;
		flexEn = ((CTRL >> 0) & 1) == 1;
//		swRst = ((CTRL >> 1) & 1) == 1;
//		fastAcc = ((CTRL >> 2) & 1) == 1;
		dbgE = ((CTRL >> 30) & 1) == 1;
		dozen = ((CTRL >> 31) & 1) == 1;
	}

	/** FLEXEN: FlexIO enable (bit 0) */
	public boolean isEnabled() {
		return flexEn;
	}

	/** DOZEN: Doze-mode enable (bit 1) */
	public boolean isDozeEnabled() {
		return dozen;
	}

	/** DBGE: Debug-enable (bit 2) */
	public boolean isDebugEnabled() {
		return dbgE;
	}

	/* ================================================================== */
	/* 						SHIFTSTAT register helpers 					  */
	/* ================================================================== */

	public int getShiftStat() {
		return SHIFTSTAT;
	}

	public void clearShiftStat(int mask) {
		SHIFTSTAT &= ~mask;
		registerMap.setRegisterValue(SHIFTSTAT_OFFSET, SHIFTSTAT);
	}

	public void setShiftStat(int mask) {
		SHIFTSTAT |= mask;
		registerMap.setRegisterValue(SHIFTSTAT_OFFSET, SHIFTSTAT);
	}

	/* ================================================================== */
	/* 						SHIFTERR register helpers 					  */
	/* ================================================================== */
	public int getShiftErr() {
		return SHIFTERR;
	}

	public void clearShiftErr(int mask) {
		SHIFTERR &= ~mask;
		registerMap.setRegisterValue(SHIFTERR_OFFSET, SHIFTERR);
	}

	public void setShiftErr(int mask) {
		SHIFTERR |= mask;
		registerMap.setRegisterValue(SHIFTERR_OFFSET, SHIFTERR);
	}

	/* ================================================================== */
	/* 						TIMSTAT register helpers 					  */
	/* ================================================================== */

	public int getTimStat() {
		return TIMSTAT;
	}

	public void clearTimStat(int mask) {
		TIMSTAT &= ~mask;
		registerMap.setRegisterValue(TIMSTAT_OFFSET, TIMSTAT);
	}

	public void setTimStat(int mask) {
		TIMSTAT |= mask;
		registerMap.setRegisterValue(TIMSTAT_OFFSET, TIMSTAT);
	}

	/* ================================================================== */
	/* 						SHIFTSIEN register 		 					  */
	/* ================================================================== */

	public int getShiftsIEN() {
		return SHIFTSIEN;
	}

	public void setShiftsIEN(int value) {
		registerMap.setRegisterValue(SHIFTSIEN_OFFSET, value);
		SHIFTSIEN = value;
	}

	/* ================================================================== */
	/* 						SHIFTEIEN register 		 					  */
	/* ================================================================== */

	public int getShiftEIEN() {
		return SHIFTEIEN;
	}

	public void setShiftEIEN(int value) {
		registerMap.setRegisterValue(SHIFTEIEN_OFFSET, value);
		SHIFTEIEN = value;
	}

	/* ================================================================== */
	/* 						TIMIEN register 		 					  */
	/* ================================================================== */

	public int getTimIEN() {
		return TIMIEN;
	}

	public void setTimIEN(int value) {
		registerMap.setRegisterValue(TIMIEN_OFFSET, value);
		TIMIEN = value;
	}

	/* ================================================================== */
	/* 						SHIFTSDEN register 		 					  */
	/* ================================================================== */

	public int getShiftSDEN() {
		return SHIFTSDEN;
	}

	public void setShiftSDEN(int value) {
		registerMap.setRegisterValue(SHIFTSDEN_OFFSET, value);
		SHIFTSDEN = value;
	}

	/* ================================================================== */
	/* 					Shifter registers helpers 						  */
	/* ================================================================== */

	public int getShiftCtl(int index) {
		return SHIFTCTL[index];
	}

	public void setShiftCtl(int index, int value) {
		registerMap.setRegisterValue(SHIFTCTL0_OFFSET + index * SHIFTER_STRIDE, value);
		SHIFTCTL[index] = value;
		shifters[index].setControlRegister(value);
	}

	public int getShiftCfg(int index) {
		return SHIFTCFG[index];
	}

	public void setShiftCfg(int index, int value) {
		registerMap.setRegisterValue(SHIFTCFG0_OFFSET + index * SHIFTER_STRIDE, value);
		SHIFTCFG[index] = value;
		shifters[index].setConfigRegister(value);
	}

	public int getShiftBuf(int index) {
		return SHIFTBUF[index];
	}

	public void setShiftBuf(int index, int value) {
		registerMap.setRegisterValue(SHIFTBUF0_OFFSET + index * SHIFTER_STRIDE, value);
		SHIFTBUF[index] = value;
		shifters[index].setBuffer(value);
	}

	public int getShiftBufBis(int index) {
		return SHIFTBUFBIS[index];
	}

	public void setShiftBufBis(int index, int value) {
		registerMap.setRegisterValue(SHIFTBUFBIS0_OFFSET + index * SHIFTER_STRIDE, value);
		SHIFTBUFBIS[index] = value;
	}

	public int getShiftBufBys(int index) {
		return SHIFTBUFBYS[index];
	}

	public void setShiftBufBys(int index, int value) {
		registerMap.setRegisterValue(SHIFTBUFBYS0_OFFSET + index * SHIFTER_STRIDE, value);
		SHIFTBUFBYS[index] = value;
	}

	public int getShiftBufBbs(int index) {
		return SHIFTBUFBBS[index];
	}

	public void setShiftBufBbs(int index, int value) {
		registerMap.setRegisterValue(SHIFTBUFBBS0_OFFSET + index * SHIFTER_STRIDE, value);
		SHIFTBUFBBS[index] = value;
	}

	/* ================================================================== */
	/* 					Timers helpers   								  */
	/* ================================================================== */

	public int getTimCtl(int index) {
		return TIMCTL[index];
	}

	public void setTimCtl(int index, int value) {
		registerMap.setRegisterValue(TIMCTL0_OFFSET + index * TIMER_STRIDE, value);
		TIMCTL[index] = value;
		timers[index].setControlRegister(value);
	}

	public int getTimCfg(int index) {
		return TIMCFG[index];
	}

	public void setTimCfg(int index, int value) {
		registerMap.setRegisterValue(TIMCFG0_OFFSET + index * TIMER_STRIDE, value);
		TIMCFG[index] = value;
		timers[index].setConfigRegister(value);
	}

	public int getTimCmp(int index) {
		return TIMCMP[index];
	}

	public void setTimCmp(int index, int value) {
		registerMap.setRegisterValue(TIMCMP0_OFFSET + index * TIMER_STRIDE, value);
		TIMCMP[index] = value;
		timers[index].setTimerCompareValue(value);
	}

	/* ================================================================== */
	/* 								Others				 				  */
	/* ================================================================== */

	public FlexIOShifter[] getShifters() {
		return shifters;
	}

	public FlexIOTimer[] getTimers() {
		return timers;
	}

	public int getShiftersCount() {
		return shiftersCount;
	}

	public int getTimersCount() {
		return timersCount;
	}

	/**
	 * Reads any FlexIO register by absolute address.
	 */
	public Integer readByAddress(int address) {
		int offset = address & 0xFFFF;
		return registerMap.getRegisterValue(offset);
	}

	/**
	 * Writes a value to a FlexIO register by absolute address.
	 */
	public void writeByAddress(int address, int value) {
		int offset = address & 0xFFFF;
		registerMap.setRegisterValue(offset, value);
		switch (offset) {
		case CTRL_OFFSET -> setCTRL(value);
		case SHIFTSTAT_OFFSET -> setShiftStat(value);
		case SHIFTERR_OFFSET -> setShiftErr(value);
		case TIMSTAT_OFFSET -> setTimStat(value);
		case SHIFTSIEN_OFFSET -> setShiftsIEN(value);
		case SHIFTEIEN_OFFSET -> setShiftEIEN(value);
		case TIMIEN_OFFSET -> setTimIEN(value);
		case SHIFTSDEN_OFFSET -> setShiftSDEN(value);

		default -> {
			/* ===== SHIFTER blocks =================================== */
			/* SHIFTCTLn */
			if (offset >= SHIFTCTL0_OFFSET && offset < SHIFTCFG0_OFFSET) {
				int idx = (offset - SHIFTCTL0_OFFSET) / SHIFTER_STRIDE;
				setShiftCtl(idx, value);
			}
			/* SHIFTCFGn */
			else if (offset >= SHIFTCFG0_OFFSET && offset < SHIFTBUF0_OFFSET) {
				int idx = (offset - SHIFTCFG0_OFFSET) / SHIFTER_STRIDE;
				setShiftCfg(idx, value);
			}
			/* SHIFTBUFn */
			else if (offset >= SHIFTBUF0_OFFSET && offset < SHIFTBUFBIS0_OFFSET) {
				int idx = (offset - SHIFTBUF0_OFFSET) / SHIFTER_STRIDE;
				setShiftBuf(idx, value);
			}
			/* SHIFTBUFBISn */
			else if (offset >= SHIFTBUFBIS0_OFFSET && offset < SHIFTBUFBYS0_OFFSET) {
				int idx = (offset - SHIFTBUFBIS0_OFFSET) / SHIFTER_STRIDE;
				setShiftBufBis(idx, value);
			}
			/* SHIFTBUFBYSn */
			else if (offset >= SHIFTBUFBYS0_OFFSET && offset < SHIFTBUFBBS0_OFFSET) {
				int idx = (offset - SHIFTBUFBYS0_OFFSET) / SHIFTER_STRIDE;
				setShiftBufBys(idx, value);
			}
			/* SHIFTBUFBBSn */
			else if (offset >= SHIFTBUFBBS0_OFFSET && offset < TIMCTL0_OFFSET) {
				int idx = (offset - SHIFTBUFBBS0_OFFSET) / SHIFTER_STRIDE;
				setShiftBufBbs(idx, value);
			}

			/* ===== TIMER blocks =================================== */
			/* TIMCTLn */
			else if (offset >= TIMCTL0_OFFSET && offset < TIMCFG0_OFFSET) {
				int idx = (offset - TIMCTL0_OFFSET) / TIMER_STRIDE;
				setTimCtl(idx, value);
			}
			/* TIMCFGn */
			else if (offset >= TIMCFG0_OFFSET && offset < TIMCMP0_OFFSET) {
				int idx = (offset - TIMCFG0_OFFSET) / TIMER_STRIDE;
				setTimCfg(idx, value);
			}
			/* TIMCMPn */
			else if (offset >= TIMCMP0_OFFSET && offset < TIMCMP0_OFFSET + timersCount * TIMER_STRIDE) {
				int idx = (offset - TIMCMP0_OFFSET) / TIMER_STRIDE;
				setTimCmp(idx, value);
			}

			else {
				// Not supported register
			}
		}
		}
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

	/**
	 * Returns the number of blocks (shifters or timers) in the FlexIO peripheral.
	 *
	 * @param firstCtlOffset The offset of the first control register of the block.
	 * @param stride         The stride between consecutive control registers.
	 * @return The number of blocks.
	 */
	private int countBlocks(int firstCtlOffset, int stride) {
		int count = 0;
		while (registerMap.containsRegister(firstCtlOffset + count * stride)) {
			count++;
		}
		return count;
	}

}
