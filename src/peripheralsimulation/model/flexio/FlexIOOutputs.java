/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.flexio;

/**
 * Enum representing the possible outputs of the FlexIO peripheral.
 * 
 * @author Veronika Lenková
 */
public enum FlexIOOutputs {

	/** Output pin 0 */
	PIN0,
	/** Shift buffer 0 */
	SHIFTBUF0,
	/** Timer 0 toggle output */
	TIMER0_TOGGLE;

	/**
	 * Returns the names of the outputs.
	 * 
	 * @return an array of output names
	 */
	public static String[] getOutputNames() {
		return new String[] { PIN0.toString(), SHIFTBUF0.toString(), TIMER0_TOGGLE.toString() };
	}
}
