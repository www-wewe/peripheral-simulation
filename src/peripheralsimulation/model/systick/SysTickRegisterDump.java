package peripheralsimulation.model.systick;

public class SysTickRegisterDump {

	private int SYST_CSR; // control and status register
	private int SYST_RVR; // reload value register
	private int SYST_CVR; // current value register
	private int SYST_CALIB; // read-only calibration info

	// CPU frequency or external frequencies
	private double mainClk; // e.g. 48e6 (48 MHz) - CPU clock
	private double externalClk; // e.g. 12e6 (12 MHz) - external (system tick timer) clock

	/**
	 * Example calculates an interrupt interval of 10 ms using external clock (12
	 * MHz). The manual states SYST_CSR=0x3 for external clock + interrupt + enable,
	 * SYST_RVR=0x1D4BF => 119999 decimal => 12MHz * 0.01s - 1 = 120000 - 1.
	 */
	public SysTickRegisterDump() {
		// SYST_CSR bits:
		// bit0=1 => ENABLE
		// bit1=1 => TICKINT
		// bit2=0 => external clock // 1 => CPU clock
		this.SYST_CSR = 0x3; // 0b11 => (ENABLE=1, TICKINT=1, CLKSOURCE=0)

		// Reload value for 10 ms at 12 MHz => 120000 - 1 = 119999
		this.SYST_RVR = 0x0001D4BF;

		// Typically we clear CVR to 0 so it restarts from RVR
		this.SYST_CVR = 0;

		// Calibration register (optional)
		this.SYST_CALIB = 0;

		// mainClk=48 MHz (CPU clock), externalClk=12 MHz
		this.mainClk = 48e6;
		this.externalClk = 12e6;
	}

	// GETTERS AND SETTERS
	public int getSYST_CSR() {
		return SYST_CSR;
	}

	public void setSYST_CSR(int SYST_CSR) {
		this.SYST_CSR = SYST_CSR;
	}

	public int getSYST_RVR() {
		return SYST_RVR;
	}

	public void setSYST_RVR(int sYST_RVR) {
		SYST_RVR = sYST_RVR;
	}

	public int getSYST_CVR() {
		return SYST_CVR;
	}

	public void setSYST_CVR(int sYST_CVR) {
		SYST_CVR = sYST_CVR;
	}

	public int getSYST_CALIB() {
		return SYST_CALIB;
	}

	public void setSYST_CALIB(int sYST_CALIB) {
		SYST_CALIB = sYST_CALIB;
	}

	public double getMainClk() {
		return mainClk;
	}

	public void setMainClk(double mainClk) {
		this.mainClk = mainClk;
	}

	public double getExternalClk() {
		return externalClk;
	}

	public void setExternalClk(double externalClk) {
		this.externalClk = externalClk;
	}

}