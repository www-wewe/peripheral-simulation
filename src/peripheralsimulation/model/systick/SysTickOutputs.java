/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.systick;

/**
 * Enum representing the possible outputs of the SysTick peripheral.
 *
 * @author Veronika Lenková
 */
public enum SysTickOutputs {

	/** The current 24-bit counter value */
	CURRENT,

	/** Whether an interrupt was generated this tick */
	INTERRUPT,

	/** The COUNTFLAG bit (bit 16 in SYST_CSR) */
	COUNTFLAG;

	/**
	 * Returns the names of the outputs.
	 * 
	 * @return an array of output names
	 */
	public static String[] getOutputNames() {
		return new String[] { CURRENT.toString(), INTERRUPT.toString(), COUNTFLAG.toString() };
	}

}
