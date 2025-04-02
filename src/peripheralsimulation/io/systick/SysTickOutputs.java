package peripheralsimulation.io.systick;

// only interrupt is real output
public enum SysTickOutputs {

	CURRENT,         // The current 24-bit counter value
    INTERRUPT_LINE,  // Whether an interrupt was generated this tick
    COUNTFLAG        // The COUNTFLAG bit (bit 16 in SYST_CSR)
}
