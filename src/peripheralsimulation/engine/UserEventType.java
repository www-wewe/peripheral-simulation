/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.engine;

/**
 * Enum representing different types of user events that can be scheduled in the
 * simulation.
 * 
 * @author Veronika Lenková
 */
public enum UserEventType {
	/**
	 * Toggles a bit in a register. The bit position is specified in the event
	 * definition.
	 */
	TOGGLE_BIT,
	/**
	 * Sets a bit in a register to 1. The bit position is specified in event
	 * definition.
	 */
	SET_BIT,
	/**
	 * Clears a bit in a register to 0. The bit position is specified in the event
	 * definition.
	 */
	CLEAR_BIT,
	/**
	 * Writes a value to a register. The register address is specified in the event
	 * definition.
	 */
	WRITE_VALUE
}
