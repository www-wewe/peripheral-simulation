/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.model.flexio.FlexIOConfig;
import peripheralsimulation.model.flexio.FlexIOOutputs;

/**
 * Extremely small Flex‑I/O behavioural model. It implements PeripheralModel so
 * it can be used by SimulationEngine, SettingsDialog, SimulationTable/Chart,
 * user events, …
 *
 * • Only TIMCTL0 / TIMCFG0 / TIMCMP0 and SHIFTCTL0 / SHIFTCFG0 are honoured. •
 * Clock source is assumed to be the FlexIO functional clock (cfg.getMainClk()).
 * • Pin direction = output, pin index = 0, one‑bit wide shifter.
 *
 * You can extend or refine the state machine later; all helper methods needed
 * by the framework are already in place.
 *
 * @author Veronika Lenková
 */
public class FlexIOModel implements PeripheralModel {

	/* ──────────────────────────────────────────────────────────────────── */
	/* Output indexes / names */
	/* ──────────────────────────────────────────────────────────────────── */

	/** Pin-0 level index */
	public static final int IDX_PIN0 = 0;
	/** Last value shifted out index */
	public static final int IDX_SHIFTBUF0 = 1;
	/** Indicates that timer-0 toggled during last update index */
	public static final int IDX_TIMER0_TOGGLE = 2;
	/** Output names (for simulation table) */
	private static final String[] OUTPUT_NAMES = FlexIOOutputs.getOutputNames();

	/* ──────────────────────────────────────────────────────────────────── */
	/* Config and internal state */
	/* ──────────────────────────────────────────────────────────────────── */
	private final FlexIOConfig config;

	/* pin-0 level (boolean for easy toggle) */
	private boolean pin0Level;

	/* last value shifted out (shows in SHIFTBUF0 output) */
	private int shiftBuf0;

	/* indicates that timer-0 toggled during last update */
	private boolean timer0Toggle;

	/* derived tick period based on TIMCFG0.PRESCALE */
	private double tickPeriod;

	/**
	 * Constructor for FlexIOModel.
	 *
	 * @param config The configuration object containing the FlexIO settings.
	 */
	public FlexIOModel(FlexIOConfig config) {
		this.config = config;
		calculateTickPeriod();
	}

	/* =================================================================== */
	/* PeripheralModel implementation */
	/* =================================================================== */

	@Override
	public void initialize(SimulationEngine engine) {
		pin0Level = false;
		shiftBuf0 = 0;
		timer0Toggle = false;

		// schedule first timer event if FlexIO is enabled + timer enabled
		if (config.isEnable() && isTimer0Enabled()) {
			engine.scheduleEvent(engine.getCurrentTime() + tickPeriod, () -> timerTick(engine));
		}
	}

	@Override
	public void update(SimulationEngine engine) {
		// TODO
	}

	/* =================================================================== */
	/* Outputs */
	/* =================================================================== */

	@Override
	public String getOutputName(int index) {
		return OUTPUT_NAMES[index];
	}

	@Override
	public String[] getOutputNames() {
		return OUTPUT_NAMES;
	}

	@Override
	public Object[] getOutputs() {
		return new Object[] { pin0Level, shiftBuf0, timer0Toggle };
	}

	@Override
	public int getOutputIndex(String name) {
		FlexIOOutputs output = FlexIOOutputs.valueOf(name);
		switch (output) {
		case PIN0:
			return IDX_PIN0;
		case SHIFTBUF0:
			return IDX_SHIFTBUF0;
		case TIMER0_TOGGLE:
			return IDX_TIMER0_TOGGLE;
		default:
			throw new IllegalArgumentException("FlexIO: unknown output " + name);
		}
	}

	/* =================================================================== */
	/* Register access used by user events */
	/* =================================================================== */

	@Override
	public void setRegisterValue(int addr, int value) {
		config.writeByAddress(addr, value);
		// If timer/shifter config changed ➜ recompute period:
		if (addr == FlexIOConfig.TIMCFG0_ADDR) {
			calculateTickPeriod();
		}
	}

	@Override
	public Integer getRegisterValue(int addr) {
		return config.readByAddress(addr);
	}

	/* =================================================================== */
	/* Internal helper methods */
	/* =================================================================== */

	private boolean isTimer0Enabled() {
		// TIMCTL.TSTART (bit 0) – very simplified
		return (config.getTIMCTL0() & 0x01) != 0;
	}

	private void calculateTickPeriod() {
		/*
		 * TIMCFG0[1:0] PRESCALE selects input clock / (2^(value)) (RM) 0 = /1, 1 = /2,
		 * 2 = /4, 3 = /8
		 */
		int prescaleBits = config.getTIMCFG0() & 0x03;
		int div = 1 << prescaleBits;

		double fclk = config.getMainClk(); // assuming main clock
		if (fclk <= 0)
			fclk = 1.0; // avoid /0
		tickPeriod = div / fclk; // seconds per timer tick
	}

	/**
	 * Called by the simulation engine when the timer is scheduled to tick.
	 * 
	 * @param engine The simulation engine instance.
	 */
	private void timerTick(SimulationEngine engine) {
		// Toggle pin, mark flag
		pin0Level = !pin0Level;
		timer0Toggle = true;

		// Very simple shifter: shift MSB first, reload on toggle high -> low
		if (pin0Level) {
			// falling edge will shift next bit
		} else {
			boolean newBit = ((shiftBuf0 & 0x80) != 0);
			pin0Level = newBit; // output bit
			shiftBuf0 = ((shiftBuf0 << 1) & 0xFF) | (newBit ? 1 : 0);
		}

		// Re-schedule next toggle while simulation is active
		if (engine.isSimulationRunning() && isTimer0Enabled()) {
			engine.scheduleEvent(engine.getCurrentTime() + tickPeriod, () -> timerTick(engine));
		}
	}
}
