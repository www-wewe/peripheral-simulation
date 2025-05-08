/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.io.UserPreferences;
import peripheralsimulation.model.flexio.FlexIOConfig;
import peripheralsimulation.model.flexio.FlexIOOutputs;
import peripheralsimulation.model.flexio.FlexIOShifter;
import peripheralsimulation.model.flexio.FlexIOTimer;

/**
 * FlexIO peripheral model, which can be simulated within the SimulationEngine.
 * Class implements the PeripheralModel interface and provides methods to
 * initialize, update, and manage the FlexIO peripheral's configuration
 *
 * <p>
 * Podporované módy:
 * </p>
 * <ul>
 * <li><b>PWM generator</b> – Timer TIMOD = 10 (dual 8-bit PWM)</li>
 * <li><b>UART TX/RX</b> – Timer TIMOD = 01 (dual 8-bit baud/bit) + Shifter SMOD
 * = Transmit/Receive</li>
 * </ul>
 *
 * @author Veronika Lenková
 */
public class FlexIOModel implements PeripheralModel {

	/** Output names */
	 private static final String[] OUTPUT_NAMES = FlexIOOutputs.getOutputNames();

	/** The configuration object with all the "register" bits */
	private final FlexIOConfig config;

	/** Clock period (in seconds) */
	private double tickPeriod = 1.0 / UserPreferences.getInstance().getClockFrequency();

	/** The timers used in the FlexIO peripheral */
	private FlexIOTimer[] timers;

	/** The shifters used in the FlexIO peripheral */
	private FlexIOShifter[] shifters;

	/**
	 * Constructor for FlexIOModel.
	 *
	 * @param config The configuration object containing the FlexIO settings.
	 */
	public FlexIOModel(FlexIOConfig config) {
		this.config = config;
		this.timers = config.getTimers();
		this.shifters = config.getShifters();
	}

	@Override
	public void initialize(SimulationEngine engine) {
		if (!config.isEnabled() || config.isDozeEnabled() || config.isDebugEnabled()) return;

		for (FlexIOTimer   t : timers)   t.reset();
        for (FlexIOShifter s : shifters) s.reset();

		scheduleEvent(engine, engine.getCurrentTime() + tickPeriod);
	}

	@Override
	public void update(SimulationEngine engine) {
		boolean[] edges = new boolean[timers.length];
		// timers
		for (int i = 0; i < timers.length; i++) {
			// decrement timer, compare with CMP, set/clear TIMSTAT bits, trigger shifters, etc.
	        edges[i] = timers[i].tick();
        }
        // shifters
        for (FlexIOShifter shifter : shifters) {
            // depending on SMOD/TIMSEL/INSRC/etc., shift bits in/out of the buffer,
            // update SHIFTSTAT/SHIFTERR, feed data to next shifter or pin, etc.
        	boolean clockEdge = edges[ shifter.getTimSel() ];
        	shifter.shift(clockEdge);
        }

		// Re-schedule next tick
		if (engine.isSimulationRunning()) {
			scheduleEvent(engine, engine.getCurrentTime() + tickPeriod);
		}
	}

	private void scheduleEvent(SimulationEngine engine, double eventTime) {
		engine.scheduleEvent(eventTime, () -> update(engine));
	}

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
		return new Object[] {  };
	}

	@Override
	public int getOutputIndex(String name) {
		for (int i = 0; i < OUTPUT_NAMES.length; ++i)
			if (OUTPUT_NAMES[i].equals(name))
				return i;
		throw new IllegalArgumentException("Unknown output " + name);
	}

	@Override
	public void setRegisterValue(int addr, int value) {
		config.writeByAddress(addr, value);
	}

	@Override
	public Integer getRegisterValue(int addr) {
		return config.readByAddress(addr);
	}

}
