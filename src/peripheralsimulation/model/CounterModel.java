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
			currentValue = 0; // overflow
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
	public int getCurrentValue() {
		return currentValue;
	}
}
