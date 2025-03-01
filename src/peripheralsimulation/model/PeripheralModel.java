package peripheralsimulation.model;

import peripheralsimulation.engine.SimulationEngine;

/**
 * Interface that all peripherals must implement in order to be simulated within
 * the SimulationEngine.
 * 
 * <p>
 * The goal is to provide a common contract for: 1. Initialization with default
 * or user-defined conditions. 2. Setting up or scheduling any time-based
 * behavior during the simulation. 3. Handling interactions with other
 * peripherals if needed.
 * </p>
 */
public interface PeripheralModel {

	/**
	 * Called by the SimulationEngine before running begins. This is where you can
	 * schedule initial events or set initial state.
	 * 
	 * @param engine The core simulation engine controlling scheduling and time.
	 */
	void initialize(SimulationEngine engine);

	/**
	 * Called by the SimulationEngine at each time step.
	 * 
	 * @param engine The core simulation engine controlling scheduling and time
	 */
	void update(SimulationEngine engine);

	/**
	 * Get the current value of the peripheral.
	 */
	int getCurrentValue();
}
