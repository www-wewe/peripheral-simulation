/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.flexio;

import java.util.Arrays;
import java.util.Map;
import static java.util.Map.entry;

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
	 * 					Register addresses (constants) 					  *
	 * ------------------------------------------------------------------ *
	 */
	public static final int FLEXIO_BASE = 0x4005_F000; // MCXN444

	public static final int VERID_ADDR = FLEXIO_BASE + 0x000;
	public static final int PARAM_ADDR = FLEXIO_BASE + 0x004;
	public static final int CTRL_ADDR = FLEXIO_BASE + 0x008;

	public static final int SHIFTSTAT_ADDR = FLEXIO_BASE + 0x010;
	public static final int SHIFTERR_ADDR = FLEXIO_BASE + 0x014;
	public static final int TIMSTAT_ADDR = FLEXIO_BASE + 0x018;

	public static final int SHIFTSIEN_ADDR = FLEXIO_BASE + 0x020;
	public static final int SHIFTEIEN_ADDR = FLEXIO_BASE + 0x024;
	public static final int TIMIEN_ADDR = FLEXIO_BASE + 0x028;
	public static final int SHIFTSDEN_ADDR = FLEXIO_BASE + 0x030;

	// --- First shifter (index 0) ---------------------------------------
	public static final int SHIFTCTL0_ADDR = FLEXIO_BASE + 0x080;
	public static final int SHIFTCFG0_ADDR = FLEXIO_BASE + 0x100;
	public static final int SHIFTBUF0_ADDR = FLEXIO_BASE + 0x200;
	public static final int SHIFTBUFBIS0_ADDR = FLEXIO_BASE + 0x280;
	public static final int SHIFTBUFBYS0_ADDR = FLEXIO_BASE + 0x300;
	public static final int SHIFTBUFBBS0_ADDR = FLEXIO_BASE + 0x380;
	public static final int SHIFTER_STRIDE = 0x004;

	// --- First timer (index 0) -----------------------------------------
	public static final int TIMCTL0_ADDR = FLEXIO_BASE + 0x400;
	public static final int TIMCFG0_ADDR = FLEXIO_BASE + 0x480;
	public static final int TIMCMP0_ADDR = FLEXIO_BASE + 0x500;
	public static final int TIMER_STRIDE = 0x004;

	/** FlexIO register names and their addresses */
	private static final Map<String,Integer> NAME2ADDR = Map.ofEntries(
			entry("FLEXIO_PARAM", PARAM_ADDR),
		    entry("FLEXIO_CTRL", CTRL_ADDR),
		    entry("FLEXIO_SHIFTCFG0", SHIFTCFG0_ADDR),
		    entry("FLEXIO_SHIFTCFG1", SHIFTCFG0_ADDR + SHIFTER_STRIDE),
		    entry("FLEXIO_SHIFTCTL0", SHIFTCTL0_ADDR),
		    entry("FLEXIO_SHIFTCTL1", SHIFTCTL0_ADDR + SHIFTER_STRIDE),
		    entry("FLEXIO_TIMCFG0", TIMCFG0_ADDR),
		    entry("FLEXIO_TIMCFG1", TIMCFG0_ADDR + TIMER_STRIDE),
		    entry("FLEXIO_TIMCMP0", TIMCMP0_ADDR),
		    entry("FLEXIO_TIMCMP1", TIMCMP0_ADDR + TIMER_STRIDE),
		    entry("FLEXIO_TIMCTL0", TIMCTL0_ADDR),
		    entry("FLEXIO_TIMCTL1", TIMCTL0_ADDR + TIMER_STRIDE)
	);

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
	private int pinCount;

	/** Jednotlivé konfigurácie: index == číslo periférie. */
	private FlexIOShifter[] shifters;
	private FlexIOTimer[] timers;

	/** Globálne riadiace bity z CTRL. */
	private boolean flexEn, swRst, fastAcc, dbgE, dozen;

	// Chip modes = Run, Stop/Wait, Low Leakage Stop, Debug

	/** Clock source frequency */
	private int clockFrequency = 8_000_000; // 8 MHz

	/** RegisterMap object to access register values. */
	private RegisterMap registerMap;

	/**
	 * Constructor for FlexIOConfig.
	 *
	 * @param registerMap The RegisterMap object containing the register addresses
	 *                    and register values.
	 */
	public FlexIOConfig(RegisterMap registerMap) {
		this.registerMap = registerMap;

		PARAM = registerMap.getRegisterValue(PARAM_ADDR);
		if (PARAM == 0) { // Default value if PARAM is not set (in MCXC444)
		    PARAM = 0x10080404; // 4 shifters, 4 timers, 8 pins
		}
		shiftersCount = PARAM & 0xFF;
		timersCount = (PARAM >> 8) & 0xFF;
		pinCount = (PARAM >> 16) & 0xFF;
		// triggersCount = (PARAM >> 24) & 0xFF;

		SHIFTCTL = new int[shiftersCount];
		SHIFTCFG = new int[shiftersCount];
		SHIFTBUF = new int[shiftersCount];
		SHIFTBUFBIS = new int[shiftersCount];
		SHIFTBUFBYS = new int[shiftersCount];
		SHIFTBUFBBS = new int[shiftersCount];
		TIMCTL = new int[timersCount];
		TIMCFG = new int[timersCount];
		TIMCMP = new int[timersCount];

		setCTRL(registerMap.getRegisterValue(CTRL_ADDR));
		SHIFTSTAT = registerMap.getRegisterValue(SHIFTSTAT_ADDR);
		SHIFTERR = registerMap.getRegisterValue(SHIFTERR_ADDR);
		TIMSTAT = registerMap.getRegisterValue(TIMSTAT_ADDR);
		SHIFTSIEN = registerMap.getRegisterValue(SHIFTSIEN_ADDR);
		SHIFTEIEN = registerMap.getRegisterValue(SHIFTEIEN_ADDR);
		TIMIEN = registerMap.getRegisterValue(TIMIEN_ADDR);
		SHIFTSDEN = registerMap.getRegisterValue(SHIFTSDEN_ADDR);

		for (int i = 0; i < shiftersCount; i++) {
			SHIFTCTL[i] = registerMap.getRegisterValue(SHIFTCTL0_ADDR + i * SHIFTER_STRIDE);
			SHIFTCFG[i] = registerMap.getRegisterValue(SHIFTCFG0_ADDR + i * SHIFTER_STRIDE);
			SHIFTBUF[i] = registerMap.getRegisterValue(SHIFTBUF0_ADDR + i * SHIFTER_STRIDE);
			SHIFTBUFBIS[i] = registerMap.getRegisterValue(SHIFTBUFBIS0_ADDR + i * SHIFTER_STRIDE);
			SHIFTBUFBYS[i] = registerMap.getRegisterValue(SHIFTBUFBYS0_ADDR + i * SHIFTER_STRIDE);
			SHIFTBUFBBS[i] = registerMap.getRegisterValue(SHIFTBUFBBS0_ADDR + i * SHIFTER_STRIDE);
		}

		for (int i = 0; i < timersCount; i++) {
			TIMCTL[i] = registerMap.getRegisterValue(TIMCTL0_ADDR + i * TIMER_STRIDE);
			TIMCFG[i] = registerMap.getRegisterValue(TIMCFG0_ADDR + i * TIMER_STRIDE);
			TIMCMP[i] = registerMap.getRegisterValue(TIMCMP0_ADDR + i * TIMER_STRIDE);
		}

		shifters = new FlexIOShifter[shiftersCount];
		timers = new FlexIOTimer[timersCount];
		Arrays.setAll(shifters, i -> new FlexIOShifter(this, i));
		Arrays.setAll(timers, i -> new FlexIOTimer(this, i));
	}

	/* ================================================================== */
	/* 						CTRL register helpers 						  */
	/* ================================================================== */

	/** Whole-register getters & setters */
	public int getCTRL() {
		return CTRL;
	}

	public void setCTRL(int value) {
		CTRL = value;
		flexEn = ((CTRL >> 0) & 1) == 1;
		swRst = ((CTRL >> 1) & 1) == 1;
		fastAcc = ((CTRL >> 2) & 1) == 1;
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
	}

	public void setShiftStat(int mask) {
		SHIFTSTAT |= mask;
	}

	/* ================================================================== */
	/* 						SHIFTERR register helpers 					  */
	/* ================================================================== */
	public int getShiftErr() {
		return SHIFTERR;
	}

	public void clearShiftErr(int mask) {
		SHIFTERR &= ~mask;
	}

	public void setShiftErr(int mask) {
		SHIFTERR |= mask;
	}

	/* ================================================================== */
	/* 						TIMSTAT register helpers 					  */
	/* ================================================================== */

	public int getTimStat() {
		return TIMSTAT;
	}

	public void clearTimStat(int mask) {
		TIMSTAT &= ~mask;
	}

	public void setTimStat(int mask) {
		TIMSTAT |= mask;
	}

	/* ================================================================== */
	/* 						SHIFTSIEN register 		 					  */
	/* ================================================================== */

	public int getShiftsIEN() {
		return SHIFTSIEN;
	}

	public void setShiftsIEN(int value) {
		SHIFTSIEN = value;
	}

	/* ================================================================== */
	/* 						SHIFTEIEN register 		 					  */
	/* ================================================================== */

	public int getShiftEIEN() {
		return SHIFTEIEN;
	}

	public void setShiftEIEN(int value) {
		SHIFTEIEN = value;
	}

	/* ================================================================== */
	/* 						TIMIEN register 		 					  */
	/* ================================================================== */

	public int getTimIEN() {
		return TIMIEN;
	}

	public void setTimIEN(int value) {
		TIMIEN = value;
	}

	/* ================================================================== */
	/* 						SHIFTSDEN register 		 					  */
	/* ================================================================== */

	public int getShiftSDEN() {
		return SHIFTSDEN;
	}

	public void setShiftSDEN(int value) {
		SHIFTSDEN = value;
	}

	/* ================================================================== */
	/* 					Shifter registers helpers 						  */
	/* ================================================================== */

	public int getShiftCtl(int index) {
		return SHIFTCTL[index];
	}

	public void setShiftCtl(int index, int value) {
		SHIFTCTL[index] = value;
		shifters[index].setControlRegister(value);
	}

	public int getShiftCfg(int index) {
		return SHIFTCFG[index];
	}

	public void setShiftCfg(int index, int value) {
		SHIFTCFG[index] = value;
		shifters[index].setConfigRegister(value);
	}

	public int getShiftBuf(int index) {
		return SHIFTBUF[index];
	}

	public void setShiftBuf(int index, int value) {
		SHIFTBUF[index] = value;
		shifters[index].setBuffer(value);
	}

	public int getShiftBufBis(int index) {
		return SHIFTBUFBIS[index];
	}

	public void setShiftBufBis(int index, int value) {
		SHIFTBUFBIS[index] = value;
	}

	public int getShiftBufBys(int index) {
		return SHIFTBUFBYS[index];
	}

	public void setShiftBufBys(int index, int value) {
		SHIFTBUFBYS[index] = value;
	}

	public int getShiftBufBbs(int index) {
		return SHIFTBUFBBS[index];
	}

	public void setShiftBufBbs(int index, int value) {
		SHIFTBUFBBS[index] = value;
	}

	/* ================================================================== */
	/* 					Timers helpers   								  */
	/* ================================================================== */

	public int getTimCtl(int index) {
		return TIMCTL[index];
	}

	public void setTimCtl(int index, int value) {
		TIMCTL[index] = value;
		timers[index].setControlRegister(value);
	}

	public int getTimCfg(int index) {
		return TIMCFG[index];
	}

	public void setTimCfg(int index, int value) {
		TIMCFG[index] = value;
		timers[index].setConfigRegister(value);
	}

	public int getTimCmp(int index) {
		return TIMCMP[index];
	}

	public void setTimCmp(int index, int value) {
		TIMCMP[index] = value;
		timers[index].setCmp(value);
	}

	/* ================================================================== */
	/* 								Others				 				  */
	/* ================================================================== */

	public FlexIOShifter[] getShifters() {
		return shifters;
	}

	public void setShifters(FlexIOShifter[] shifters) {
		this.shifters = shifters;
	}

	public FlexIOTimer[] getTimers() {
		return timers;
	}

	public void setTimers(FlexIOTimer[] timers) {
		this.timers = timers;
	}

	public int getShiftersCount() {
		return shiftersCount;
	}

	public int getTimersCount() {
		return timersCount;
	}

	/**
	 * Reads any FlexIO register by absolute address
	 */
	public Integer readByAddress(int addr) {
		return registerMap.getRegisterValue(addr);
	}

	/**
	 * Writes a value to a FlexIO register by absolute address
	 */
	public void writeByAddress(int addr, int value) {
		registerMap.setRegisterValue(addr, value);
		// TODO: update the register value in the FlexIOConfig object
	}

	public int getClockFrequency() {
		return clockFrequency;
	}

	public void setClockFrequency(int clockFrequency) {
		this.clockFrequency = clockFrequency;
	}

	/**
	 * Returns the address of a register by its name.
	 *
	 * @param name The name of the register.
	 * @return The address of the register, or -1 if not found.
	 */
	public static int getRegisterAddress(String name) {
		return NAME2ADDR.getOrDefault(name, -1);
	}

}
