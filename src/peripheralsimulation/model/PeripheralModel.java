package peripheralsimulation.model;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.engine.UserEvent;
import peripheralsimulation.engine.UserEventType;

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
	 * Returns the name for a given output index. e.g. 0 -> "CURRENT", 1 ->
	 * "INTERRUPT", 2 -> "COUNTFLAG"
	 */
	String getOutputName(int index);

	/**
	 * Returns the index of a given output name. e.g. "CURRENT" -> 0, "INTERRUPT" ->
	 * 1, "COUNTFLAG" -> 2
	 */
	int getOutputIndex(String name);

	/**
	 * Returns an array of objects representing the current state of the peripheral.
	 * 
	 * @return an array of peripheral outputs
	 */
	Object[] getOutputs();

	/**
	 * Returns the names of all outputs for this peripheral.
	 * 
	 * @return an array of output names
	 */
	String[] getOutputNames();

	/**
	 * Sets the value of a register by its address.
	 *
	 * @param registerAddress The address of the register to set.
	 * @param value           The value to set the register to.
	 */
	void setRegisterValue(int registerAddress, int value);

	/**
	 * Gets the value of a register by its address.
	 *
	 * @param registerAddress The address of the register to get.
	 * @return The value of the register, or null if not found.
	 */
	Integer getRegisterValue(int registerAddress);

	/**
	 * Applies a user event to the peripheral.
	 * 
	 * @param event to apply
	 */
	default public void applyUserEvent(UserEvent event) {
		if (event.eventType == UserEventType.WRITE_VALUE) {
			setRegisterValue(event.registerAddress, event.value);
		} else {
			setBit(event);
		}
	}

	/**
	 * Sets or clears a specific bit in the register. True means set the bit, false
	 * means clear the bit.
	 *
	 * @param bitPosition The position of the bit to set (0-31).
	 * @param newVal      The new value for the bit (true for set, false for clear).
	 */
	private void setBit(UserEvent event) {
		Integer registerValue = getRegisterValue(event.registerAddress);
		if (registerValue != null) {
			int mask = 1 << event.bitPosition;
			switch (event.eventType) {
			case SET_BIT:
				registerValue |= mask; // Set the bit
				break;
			case CLEAR_BIT:
				registerValue &= ~mask; // Clear the bit
				break;
			case TOGGLE_BIT:
				registerValue ^= mask; // Toggle the bit
				break;
			default:
				throw new IllegalArgumentException("Invalid event type for setting bit: " + event.eventType);
			}
			setRegisterValue(event.registerAddress, registerValue);
		} else {
			throw new IllegalArgumentException("Register not found for address: " + event.registerAddress);
		}
	}

}
