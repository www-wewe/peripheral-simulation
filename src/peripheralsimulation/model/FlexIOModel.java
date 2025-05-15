/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model;

import java.util.ArrayList;
import java.util.List;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.io.UserPreferences;
import peripheralsimulation.model.flexio.Edge;
import peripheralsimulation.model.flexio.FlexIOConfig;
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
	private final String[] outputNames;

	/** The configuration object with all the "register" bits */
	private final FlexIOConfig config;

	/** Clock period (in seconds) */
	private double tickPeriod;

	/** The timers used in the FlexIO peripheral */
	private FlexIOTimer[] timers;

	/** The shifters used in the FlexIO peripheral */
	private FlexIOShifter[] shifters;

	/** Number of timers */
	private int timersCount;

	/** Number of shifters */
	private int shifterCount;

	/**
	 * Constructor for FlexIOModel.
	 *
	 * @param config The configuration object containing the FlexIO settings.
	 */
	public FlexIOModel(FlexIOConfig config) {
		this.config = config;
		this.timers = config.getTimers();
		this.shifters = config.getShifters();

		/* Create output names: first all TIMER outputs, then SHIFTER pins */
		List<String> names = new ArrayList<>();
		for (int i = 0; i < timers.length; i++) {
			names.add("Timer" + i + "_OUT"); // output level
			names.add("Timer" + i + "_CNT"); // counter
			names.add("Timer" + i + "_TSF"); // status flag
		}
		for (int i = 0; i < shifters.length; i++) {
			names.add("Shifter" + i + "_PIN"); // pin level
			names.add("Shifter" + i + "_SSF"); // status flag
			names.add("Shifter" + i + "_SEF"); // error flag
		}

		outputNames = names.toArray(String[]::new);
		timersCount = timers.length;
		shifterCount = shifters.length;
	}

	@Override
	public void initialize(SimulationEngine engine) {
		tickPeriod = 1.0 / UserPreferences.getInstance().getClockFrequency();
		if (!config.isEnabled() || config.isDozeEnabled() || config.isDebugEnabled())
			return;

		config.clearRuntimeFlags();

		for (FlexIOTimer timer : timers)
			timer.reset();
		for (FlexIOShifter shifter : shifters)
			shifter.reset();

		scheduleEvent(engine, engine.getCurrentTime() + tickPeriod);
	}

	@Override
	public void update(SimulationEngine engine) {
		Edge[] edges = new Edge[timers.length];
		// timers
		for (int i = 0; i < timers.length; i++) {
			// decrement timer, compare with CMP, set/clear TIMSTAT bits, trigger shifters,
			// etc.
			edges[i] = timers[i].tick();
		}
		// shifters
		for (FlexIOShifter shifter : shifters) {
			// depending on SMOD/TIMSEL/INSRC/etc., shift bits in/out of the buffer,
			// update SHIFTSTAT/SHIFTERR, feed data to next shifter or pin, etc.
			Edge clockEdge = edges[shifter.getTimerSelect()];
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
		return outputNames[index];
	}

	@Override
	public String[] getOutputNames() {
		return outputNames;
	}

	@Override
	public Object[] getOutputs() {
		Object[] outputs = new Object[(timersCount + shifterCount) * 3];
		int k = 0;

		/* TIMERy */
		for (int i = 0; i < timersCount; i++) {
			FlexIOTimer timer = timers[i];
			outputs[k++] = timer.isClockLevelHigh() ? 1 : 0; // _OUT
			outputs[k++] = timer.getCurrentCounter(); // _CNT
			outputs[k++] = timer.isStatusFlagSet() ? 1 : 0; // _TSF
		}

		/* SHIFTERy */
		for (int i = 0; i < shifterCount; i++) {
			FlexIOShifter shifter = shifters[i];
			outputs[k++] = shifter.isPinLevelHigh() ? 1 : 0; // _PIN
			outputs[k++] = shifter.isStatusFlagSet() ? 1 : 0; // _SSF
			outputs[k++] = shifter.isErrorFlagSet() ? 1 : 0; // _SEF
		}
		config.clearRuntimeFlags();
		return outputs;
	}

	@Override
	public int getOutputIndex(String name) {
		for (int i = 0; i < outputNames.length; ++i)
			if (outputNames[i].equals(name))
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

	@Override
	public Peripheral getPeripheralType() {
		return Peripheral.FLEXIO;
	}

}
