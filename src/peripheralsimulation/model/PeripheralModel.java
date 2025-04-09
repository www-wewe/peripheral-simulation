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
	 * Called by the SimulationEngine at each time step. Schedule next increment
	 * only if the simulation is running. Use
	 * {@link SimulationEngine.isSimulationRunning()}.
	 * 
	 * @param engine The core simulation engine controlling scheduling and time
	 */
	void update(SimulationEngine engine);

	/**
	 * Returns the total number of outputs this peripheral exposes. 
	 * e.g. for SysTick => 3 outputs (CURRENT, INTERRUPT_LINE, COUNTFLAG)
	 */
	int getOutputCount();

	/**
	 * Returns the name for a given output index.
	 * e.g. 0 -> "CURRENT", 1 -> "INTERRUPT_LINE", 2 -> "COUNTFLAG"
	 */
	String getOutputName(int index);

	/**
	 * Returns the index of a given output name. e.g. "CURRENT" -> 0,
	 * "INTERRUPT_LINE" -> 1, "COUNTFLAG" -> 2
	 */
	int getOutputIndex(String name);

	/**
	 * Returns an array of objects representing the current state of the peripheral.
	 * @return an array of peripheral outputs
	 */
	Object[] getOutputs();

	/**
	 * Returns the names of all outputs for this peripheral.
	 * @return an array of output names
	 */
	String[] getOutputNames();
}
