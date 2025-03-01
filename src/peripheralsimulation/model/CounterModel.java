package peripheralsimulation.model;

import peripheralsimulation.engine.SimulationEngine;

/**
 * A simple counter model that increments at a fixed interval and wraps back to
 * zero upon reaching an overflow value.
 */
public class CounterModel implements PeripheralModel {

	private int overflowValue; // e.g., 255 for 8-bit, 65535 for 16-bit, etc.
	private int increment; // increment of current value each time step
	private int currentValue; // internal counter
	private double timeStep; // e.g., 1.0 for each iteration

	public CounterModel(int overflowValue, int increment, int initialValue, double timeStep) {
		this.overflowValue = overflowValue;
		this.increment = increment;
		this.currentValue = initialValue;
		this.timeStep = timeStep;
	}

	@Override
	public void initialize(SimulationEngine engine) {
		// Schedule the first increment
		scheduleIncrement(engine, engine.getCurrentTime() + timeStep);
	}

	private void scheduleIncrement(SimulationEngine engine, double eventTime) {
		engine.scheduleEvent(eventTime, () -> update(engine));
	}

	@Override
	public void update(SimulationEngine engine) {
		// Increment logic
		currentValue += increment;
		if (currentValue > overflowValue) {
			currentValue = 0; // Overflow resets to zero
		}

		System.out.println("[CounterPeripheral] time=" + engine.getCurrentTime() +
				", currentValue=" + currentValue);

		// Next increment
		double nextTime = engine.getCurrentTime() + timeStep;
		scheduleIncrement(engine, nextTime);
	}

	@Override
	public int getCurrentValue() {
		return currentValue;
	}

}
