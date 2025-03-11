package peripheralsimulation.model;

import java.util.Map;

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
	 * Schedule next increment only if the simulation is running.
	 * Use {@link SimulationEngine.isSimulationRunning()}.
	 * 
	 * @param engine The core simulation engine controlling scheduling and time
	 */
	void update(SimulationEngine engine);

	/**
     * Returns a map of named outputs so the UI can select which to display.
     * The keys can be e.g. "CURRENT", "INTERRUPT", etc.
     * The values can be integers, booleans, strings, or whatever is relevant.
     */
    Map<String, Object> getOutputs();
}
