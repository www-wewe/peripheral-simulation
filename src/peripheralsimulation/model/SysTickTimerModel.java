package peripheralsimulation.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.io.systick.SysTickTimerConfig;

/**
 * Simulates an ARM SysTick Timer (24-bit), with basic registers: - SYST_CSR
 * (ENABLE, TICKINT, CLKSOURCE, COUNTFLAG) - SYST_RVR (Reload Value) - SYST_CVR
 * (Current Value) - SYST_CALIB (optional read-only info)
 */
public class SysTickTimerModel implements PeripheralModel {

	/* The configuration object with all the "register" bits */
	private final SysTickTimerConfig config;

	/* The current value of the counter */
	private int currentValue;

	/* COUNTFLAG bit (auto-clear on read or reload) */
	private boolean countFlag;

	/* For highlighting "interrupt triggered" in UI */
	private boolean interruptLine;

	/* Derived field (time per tick). Could be re-computed if config changes */
	private double tickPeriod;

	/**
	 * Construct SysTick with some initial config, e.g. from SysTickConfig
	 */
	public SysTickTimerModel(SysTickTimerConfig config) {
		this.config = config;
		this.currentValue = 0; // will get set in initialize() or on write
		this.countFlag = false;
		this.interruptLine = false;

		// Pre-calculate your tickPeriod once (or each time config changes):
		this.tickPeriod = calculateTickPeriod();
	}

	/**
	 * Calculate the tick period based on the configuration
	 * 
	 * @param config
	 */
	private double calculateTickPeriod() {
		double freq = config.useCpuClock ? config.cpuClockFreq : config.externalFreq;
		int prescaler = (config.prescalerDiv <= 0) ? 1 : config.prescalerDiv;
		freq = (freq <= 0) ? 1 : freq;
		return 1.0 / (freq / prescaler);
	}

	@Override
	public void initialize(SimulationEngine engine) {
		// Writing to SYST_CVR sets it to 0 and clears COUNTFLAG
		currentValue = config.reloadValue & 0x00FFFFFF;
		countFlag = false;
		interruptLine = false;

		// If the timer is enabled, schedule the first decrement
		if (config.enable) {
			scheduleNextDecrement(engine, engine.getCurrentTime() + tickPeriod);
		}
	}

	@Override
	public void update(SimulationEngine engine) {
		// Decrement only if enabled
		if (config.enable) {
			currentValue--; // 24-bit down counter
			if (currentValue < 0) {
				// Underflow => reload from SYST_RVR
				currentValue = config.reloadValue & 0x00FFFFFF;

				// COUNTFLAG bit => set to true once we underflow
				countFlag = true;

				// If TICKINT=1 => raise interrupt
				if (config.tickInt) {
					interruptLine = true;
				}
			} else {
				countFlag = false;
			}

			// Re-schedule next tick
			if (engine.isSimulationRunning()) {
				scheduleNextDecrement(engine, engine.getCurrentTime() + tickPeriod);
			}
		}

	}

	private void scheduleNextDecrement(SimulationEngine engine, double eventTime) {
		engine.scheduleEvent(eventTime, () -> update(engine));
	}

	/**
	 * Read the current SysTick value
	 * 
	 * @return current value of SYST_CVR
	 */
	public int readCVR() {
		// reading SYST_CVR doesn't clear it in real SysTick
		return currentValue & 0x00FFFFFF;
	}

	/**
	 * "write" to SYST_CVR. Writing any value clears the System Tick counter and the
	 * COUNTFLAG bit in SYST_CSR.
	 * 
	 * @param value to write to SYST_CVR
	 */
	public void writeCVR(int value) {
		// writing any value =>
		// sets SYST_CVR to 0, clears COUNTFLAG
		currentValue = 0;
		countFlag = false;
	}

	/**
	 * "write" to SYST_CSR,
	 * 
	 * @param enable
	 * @param tickInt
	 * @param clksource
	 */
	public void writeCSR(boolean enable, boolean tickInt, boolean clksource) {
		config.enable = enable;
		config.tickInt = tickInt;
		config.useCpuClock = clksource;

		// re-calculate tickPeriod
		this.tickPeriod = calculateTickPeriod();
	}

	/**
	 * Get the COUNTFLAG (auto-clears on read in real hardware)
	 * 
	 * @return true if COUNTFLAG was set
	 */
	public boolean readCountFlag() {
		boolean temp = countFlag;
		countFlag = false; // hardware auto-clear
		return temp;
	}

	/**
	 * "interrupt" read
	 * 
	 * @return true if interrupt
	 */
	public boolean isInterruptLine() {
		boolean temp = interruptLine;
		interruptLine = false; // clear after reading for next cycle
		return temp;
	}

	@Override
	public Map<String, Object> getOutputValues() {
		Map<String, Object> out = new HashMap<>();
		out.put("CURRENT", readCVR());
		out.put("INTERRUPT_LINE", isInterruptLine());
		out.put("COUNTFLAG", readCountFlag());
		return out;
	}

	@Override
	public Set<String> getOutputs() {
		return Set.of("CURRENT", "INTERRUPT_LINE", "COUNTFLAG");
	}

}
