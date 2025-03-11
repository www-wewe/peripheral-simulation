package peripheralsimulation.model;

import java.util.HashMap;
import java.util.Map;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.io.systick.SysTickTimerConfig;

/**
 * Simulates an ARM SysTick Timer (24-bit), with basic registers: - SYST_CSR
 * (ENABLE, TICKINT, CLKSOURCE, COUNTFLAG) - SYST_RVR (Reload Value) - SYST_CVR
 * (Current Value) - SYST_CALIB (optional read-only info)
 */
public class SysTickTimerModel implements PeripheralModel {

	// Main 24-bit registers
	private int systRVR; // reload register (24-bit)
	private int systCVR; // current counter (24-bit)

	// SYST_CSR bits:
	private boolean enable; // ENABLE bit
	private boolean tickInt; // TICKINT bit
	private boolean useCpuClock;// CLKSOURCE bit
	private boolean countFlag; // COUNTFLAG bit (auto-clear on read or reload)

	// Simulation-based: clock freq, time per tick, etc.
	private double clockFreq; // e.g. CPU clock or external
	private int prescalerDiv; // optional extra divider
	private double tickPeriod; // time for one SysTick decrement

	// For highlighting "interrupt triggered"
	private boolean interruptLine;

	/**
	 * Construct SysTick with some initial config, e.g. from SysTickConfig
	 */
	public SysTickTimerModel(SysTickTimerConfig config) {
		this.enable = config.enable;
		this.tickInt = config.tickInt;
		this.useCpuClock = config.useCpuClock;
		this.systRVR = config.reloadValue & 0x00FFFFFF; // 24-bit mask
		this.systCVR = 0; // will get set in initialize() or on write

		// Choose clock freq
		if (useCpuClock) {
			this.clockFreq = config.cpuClockFreq;
		} else {
			this.clockFreq = config.externalFreq;
		}
		this.prescalerDiv = config.prescalerDiv <= 0 ? 1 : config.prescalerDiv;

		// Example: each tick is 1/(clockFreq/prescaler)
		this.tickPeriod = 1.0 / (clockFreq / prescalerDiv);
	}

	@Override
	public void initialize(SimulationEngine engine) {
		// Typically writing to SYST_CVR sets it to 0 and clears COUNTFLAG
		systCVR = systRVR;
		countFlag = false;
		interruptLine = false;

		// If the timer is enabled, schedule the first decrement
		if (enable) {
			scheduleNextDecrement(engine, engine.getCurrentTime() + tickPeriod);
		}
	}

	@Override
	public void update(SimulationEngine engine) {
		// Decrement only if enabled
		if (enable) {
			systCVR--; // 24-bit down counter
			if (systCVR < 0) {
				// Underflow => reload from SYST_RVR
				systCVR = systRVR;

				// COUNTFLAG bit => set to true once we underflow
				countFlag = true;

				// If TICKINT=1 => raise interrupt
				if (tickInt) {
					interruptLine = true;
					// In a real system, interruptLine might cause the CPU to run the SysTick ISR
					// We could also schedule an "interrupt event" in the SimulationEngine if needed
				}
			} else {
				countFlag = false;
			}

			// Re-schedule next tick
			scheduleNextDecrement(engine, engine.getCurrentTime() + tickPeriod);
		}

	}

	private void scheduleNextDecrement(SimulationEngine engine, double eventTime) {
		if (engine.isSimulationRunning()) {
			engine.scheduleEvent(eventTime, () -> update(engine));
		}
	}

	/**
	 * Read the current SysTick value
	 * 
	 * @return current value of SYST_CVR
	 */
	public int readCVR() {
		// reading SYST_CVR doesn't clear it in real SysTick
		return systCVR & 0x00FFFFFF;
	}

	/**
	 * User might call this to "write" to SYST_CVR.
	 * 
	 * @param value to write to SYST_CVR
	 */
	public void writeCVR(int value) {
		// writing any value =>
		// sets SYST_CVR to 0, clears COUNTFLAG
		systCVR = 0;
		countFlag = false;
	}

	/**
	 * User might call this to "write" to SYST_CSR,
	 * 
	 * @param enable
	 * @param tickInt
	 * @param clksource
	 */
	public void writeCSR(boolean enable, boolean tickInt, boolean clksource) {
		this.enable = enable;
		this.tickInt = tickInt;
		this.useCpuClock = clksource;
		// If we just turned enable on, we might re-schedule
		// If we turned it off, we might skip scheduling
		// ...
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
	public Map<String, Object> getOutputs() {
		Map<String, Object> out = new HashMap<>();
		out.put("CURRENT", systCVR);
		out.put("INTERRUPT_LINE", interruptLine);
		out.put("COUNTFLAG", countFlag);
		return out;
	}

}
