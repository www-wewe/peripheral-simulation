package peripheralsimulation.model;

import peripheralsimulation.engine.SimulationEngine;

/**
 * Simulates a hardware counter incrementing at a given clock frequency with a
 * prescaler. Once it exceeds 'overflowValue', it resets to zero (overflow).
 */
public class CounterModel implements PeripheralModel {

	private int overflowValue; // e.g., 255 for 8-bit
	private int currentValue; // internal counter
	private double tickPeriod; // time between increments = 1/(clockFreq/prescaler)
	private boolean justOverflowed; // flag to indicate overflow

	/**
	 * 
	 * @param overflowValue Max counter value before overflow (e.g. 255 for 8-bit).
	 * @param initialValue  Starting value of the counter.
	 * @param clockFreq     Frequency of the counter clock in Hz (e.g., 1000.0 => 1
	 *                      kHz).
	 * @param prescaler     Clock prescaler (e.g., 1, 2, 4, 8...), divides the clock
	 *                      freq. The effective freq => clockFreq / prescaler.
	 */
	public CounterModel(int overflowValue, int initialValue, double clockFreq, int prescaler) {
		this.overflowValue = overflowValue;
		this.currentValue = initialValue;

		// Each increment occurs every (1 / (clockFreq / prescaler)) time units
		this.tickPeriod = 1.0 / (clockFreq / prescaler);
		this.justOverflowed = false;
	}

	@Override
	public void initialize(SimulationEngine engine) {
		// Schedule the first increment
		scheduleNextIncrement(engine, engine.getCurrentTime() + tickPeriod);
	}

	@Override
	public void update(SimulationEngine engine) {
		// Increment the counter
		currentValue++;
		if (currentValue > overflowValue) {
			currentValue = 0; //
			justOverflowed = true;
		}

		System.out.println("[CounterModel] time=" + engine.getCurrentTime() + ", currentValue=" + currentValue);

		// Schedule next increment
		if (engine.isSimulationRunning()) {
			scheduleNextIncrement(engine, engine.getCurrentTime() + tickPeriod);
		}
	}

	private void scheduleNextIncrement(SimulationEngine engine, double eventTime) {
		engine.scheduleEvent(eventTime, () -> update(engine));
	}

	@Override
	public int getOutputCount() {
		return 2; // CURRENT_VALUE, OVERFLOW_OCCURRED
	}

	@Override
	public String getOutputName(int index) {
		switch (index) {
		case 0:
			return "CURRENT_VALUE";
		case 1:
			return "OVERFLOW_OCCURRED";
		default:
			throw new IllegalArgumentException("Invalid output index");
		}
	}

	@Override
	public String[] getOutputNames() {
		return new String[] { "CURRENT_VALUE", "OVERFLOW_OCCURRED" };
	}

	@Override
	public Object[] getOutputs() {
		return new Object[] { currentValue, (currentValue == 0) && justOverflowed };
	}

	@Override
	public int getOutputIndex(String name) {
		switch (name) {
		case "CURRENT_VALUE":
			return 0;
		case "OVERFLOW_OCCURRED":
			return 1;
		default:
			throw new IllegalArgumentException("Invalid output name: " + name);
		}
	}

//	@Override
//	public Map<String, Object> getOutputValues() {
//		Map<String, Object> out = new HashMap<>();
//		out.put("CURRENT_VALUE", currentValue);
//		out.put("OVERFLOW_OCCURRED", (currentValue == 0) && justOverflowed);
//		return out;
//	}
//
//	@Override
//	public Set<String> getOutputs() {
//		return Set.of("CURRENT_VALUE", "OVERFLOW_OCCURRED");
//	}
}
