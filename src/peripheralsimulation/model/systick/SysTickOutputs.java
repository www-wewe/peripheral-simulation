package peripheralsimulation.model.systick;

// only interrupt is real output
public enum SysTickOutputs {

	CURRENT, // The current 24-bit counter value
	INTERRUPT, // Whether an interrupt was generated this tick
	COUNTFLAG; // The COUNTFLAG bit (bit 16 in SYST_CSR)

	public static String[] getOutputNames() {
		return new String[] { CURRENT.toString(), INTERRUPT.toString(), COUNTFLAG.toString() };
	}
}
