package peripheralsimulation.model;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.model.systick.SysTickOutputs;
import peripheralsimulation.model.systick.SysTickTimerConfig;
import peripheralsimulation.utils.RegisterUtils;

/**
 * Simulates an ARM SysTick Timer (24-bit), with basic registers: - SYST_CSR
 * (ENABLE, TICKINT, CLKSOURCE, COUNTFLAG) - SYST_RVR (Reload Value) - SYST_CVR
 * (Current Value) - SYST_CALIB (optional read-only info)
 */
public class SysTickTimerModel implements PeripheralModel {

	/* Register addresses */
	private static final int SYST_CSR_ADDR = 0xE000E010;
	private static final int SYST_RVR_ADDR = 0xE000E014;
	private static final int SYST_CVR_ADDR = 0xE000E018;
	private static final int SYST_CALIB_ADDR = 0xE000E01C;

	/* Output indices */
	public static final int IDX_CURRENT = 0;
	public static final int IDX_INTERRUPT = 1;
	public static final int IDX_COUNTFLAG = 2;

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
		return (freq <= 0) ? 1.0 : (1.0 / freq);
	}

	@Override
	public void initialize(SimulationEngine engine) {
		// Writing to SYST_CVR sets it to 0 and clears COUNTFLAG
		currentValue = config.reloadValue & RegisterUtils.BIT_MASK;
		countFlag = false;
		isInterrupt = false;

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
				currentValue = config.reloadValue & RegisterUtils.BIT_MASK;

				// COUNTFLAG bit => set to true once we underflow
				countFlag = true;

				// If TICKINT=1 => raise interrupt
				if (config.tickInt) {
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
		switch (name) {
		case "CURRENT":
			return IDX_CURRENT;
		case "INTERRUPT":
			return IDX_INTERRUPT;
		case "COUNTFLAG":
			return IDX_COUNTFLAG;
		default:
			throw new IllegalArgumentException("Invalid output name: " + name);
		}
	}

	@Override
	public void setRegisterValue(int registerAddress, int value) {
		switch (registerAddress) {
		case SYST_CSR_ADDR:
			writeCSR((value & 0x1) != 0, (value & 0x2) != 0, (value & 0x4) != 0);
			break;
		case SYST_RVR_ADDR:
			config.reloadValue = value & RegisterUtils.BIT_MASK;
			break;
		case SYST_CVR_ADDR:
			writeCVR(value);
			break;
		case SYST_CALIB_ADDR:
			throw new UnsupportedOperationException("Cannot write to read-only register: " + registerAddress);
		default:
			throw new IllegalArgumentException("Invalid register address: " + registerAddress);
		}
	}

	@Override
	public Integer getRegisterValue(int registerAddress) {
		switch (registerAddress) {
		case SYST_CSR_ADDR:
			return config.getSYST_CSR();
		case SYST_RVR_ADDR:
			return config.getSYST_RVR();
		case SYST_CVR_ADDR:
			return readCVR();
		case SYST_CALIB_ADDR:
			return config.getSYST_CALIB();
		default:
			throw new IllegalArgumentException("Invalid register address: " + registerAddress);
		}
	}

}
