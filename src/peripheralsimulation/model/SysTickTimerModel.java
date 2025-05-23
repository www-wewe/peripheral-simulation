/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.io.UserPreferences;
import peripheralsimulation.model.systick.SysTickOutputs;
import peripheralsimulation.model.systick.SysTickTimerConfig;
import peripheralsimulation.utils.RegisterUtils;

/**
 * Simulates an ARM SysTick Timer (24-bit), with basic registers: - SYST_CSR
 * (ENABLE, TICKINT, CLKSOURCE, COUNTFLAG) - SYST_RVR (Reload Value) - SYST_CVR
 * (Current Value) - SYST_CALIB (optional read-only info)
 *
 * @author Veronika Lenková
 */
public class SysTickTimerModel implements PeripheralModel {

	/* Output indices */
	private static final int IDX_CURRENT = 0;
	private static final int IDX_INTERRUPT = 1;
	private static final int IDX_COUNTFLAG = 2;

	/* Output names */
	private static final String[] OUTPUT_NAMES = SysTickOutputs.getOutputNames();

	/* The configuration object with all the "register" bits */
	private final SysTickTimerConfig config;

	/* The current value of the counter */
	private int currentValue;

	/* COUNTFLAG bit (auto-clear on read or reload) */
	private boolean countFlag;

	/* For highlighting "interrupt triggered" in UI */
	private boolean isInterrupt;

	/* Derived field (time per tick). Could be re-computed if config changes */
	private double tickPeriod;

	/**
	 * Construct SysTick with some initial config, e.g. from SysTickConfig
	 */
	public SysTickTimerModel(SysTickTimerConfig config) {
		this.config = config;
		this.currentValue = 0; // will get set in initialize() or on write
		this.countFlag = false;
		this.isInterrupt = false;
	}

	/**
	 * Calculate the tick period based on the configuration
	 */
	private double calculateTickPeriod() {
		double freq = config.isUseCpuClock() ? UserPreferences.getInstance().getClockFrequency()
				: UserPreferences.getInstance().getExternalClockFrequency();
		return (freq <= 0) ? 1.0 : (1.0 / freq);
	}

	@Override
	public void initialize(SimulationEngine engine) {
		// Writing to SYST_CVR sets it to 0 and clears COUNTFLAG
		currentValue = config.getRVR();
		countFlag = false;
		isInterrupt = false;
		this.tickPeriod = calculateTickPeriod();

		// If the timer is enabled, schedule the first decrement
		if (config.isEnabled()) {
			scheduleNextDecrement(engine, engine.getCurrentTime() + tickPeriod);
		}
	}

	@Override
	public void update(SimulationEngine engine) {
		// Decrement only if enabled
		if (config.isEnabled()) {
			currentValue--; // 24-bit down counter
			if (currentValue < 0) {
				// Underflow => reload from SYST_RVR
				currentValue = config.getRVR();

				// COUNTFLAG bit => set to true once we underflow
				countFlag = true;

				// If TICKINT=1 => raise interrupt
				if (config.isTickInt()) {
					isInterrupt = true;
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

	/**
	 * Schedule the next decrement of the SysTick timer
	 *
	 * @param engine    The simulation engine to use for scheduling.
	 * @param eventTime The time at which the next decrement should occur.
	 */
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
		return currentValue & RegisterUtils.BIT_MASK;
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
		config.setCVR(value);
	}

	/**
	 * "write" to SYST_CSR,
	 *
	 * @param enable
	 * @param tickInt
	 * @param clksource
	 */
	public void writeCSR(boolean enable, boolean tickInt, boolean clksource) {
		config.setEnabled(enable);
		config.setTickInt(tickInt);
		config.setUseCpuClock(clksource);

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
	 * @return true if interrupt was generated
	 */
	public boolean isInterruptGenerated() {
		boolean temp = isInterrupt;
		isInterrupt = false; // clear after reading for next cycle
		return temp;
	}

	@Override
	public String getOutputName(int index) {
		return OUTPUT_NAMES[index];
	}

	@Override
	public Object[] getOutputs() {
		return new Object[] { readCVR(), isInterruptGenerated(), readCountFlag() };
	}

	@Override
	public String[] getOutputNames() {
		return OUTPUT_NAMES;
	}

	@Override
	public int getOutputIndex(String name) {
		SysTickOutputs output = SysTickOutputs.valueOf(name);
		switch (output) {
		case CURRENT:
			return IDX_CURRENT;
		case INTERRUPT:
			return IDX_INTERRUPT;
		case COUNTFLAG:
			return IDX_COUNTFLAG;
		default:
			throw new IllegalArgumentException("Invalid output name: " + name);
		}
	}

	@Override
	public void setRegisterValue(int registerAddress, int value) {
		int offset = registerAddress & 0xfff;
		switch (offset) {
		case SysTickTimerConfig.CSR_OFFSET:
			config.setCSR(value);
			this.tickPeriod = calculateTickPeriod();
			break;
		case SysTickTimerConfig.RVR_OFFSET:
			config.setRVR(value);
			break;
		case SysTickTimerConfig.CVR_OFFSET:
			writeCVR(value);
			break;
		case SysTickTimerConfig.CALIB_OFFSET:
			throw new UnsupportedOperationException("Cannot write to read-only register: " + registerAddress);
		default:
			throw new IllegalArgumentException("Invalid register address: " + registerAddress);
		}
	}

	@Override
	public Integer getRegisterValue(int registerAddress) {
		int offset = registerAddress & 0xfff;
		switch (offset) {
		case SysTickTimerConfig.CSR_OFFSET:
			return config.getCSR();
		case SysTickTimerConfig.RVR_OFFSET:
			return config.getRVR();
		case SysTickTimerConfig.CVR_OFFSET:
			return readCVR();
		case SysTickTimerConfig.CALIB_OFFSET:
			return config.getCALIB();
		default:
			throw new IllegalArgumentException("Invalid register address: " + registerAddress);
		}
	}

	@Override
	public Peripheral getPeripheralType() {
		return Peripheral.SYSTICKTIMER;
	}

}
