package peripheralsimulation.io.systick;

public class SysTickRegisterDump {

	private int SYST_CSR; // control and status register
	private int SYST_RVR; // reload value register
	private int SYST_CVR; // current value register
	private int SYST_CALIB; // read-only calibration info

	// CPU frequency or external frequencies
	private double mainClk; // e.g. 48e6 (48 MHz) - CPU clock
	private double externalClk; // e.g. 12e6 (12 MHz) - external (system tick timer) clock

	// MCUXpresso clock config
	// SYSCON->SYSTICKCLKDIV0,DIV,0, SYSCON->SYSTICKCLKSEL0,SEL,0 ...
	private int SYSTICKCLKDIV0;
	private int SYSTICKCLKSEL0;
	private int SYSTICKCLKDIV1;
	private int SYSTICKCLKSEL1;

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

		// Dividers and selectors:
		// If we want no division, SYSTICKCLKDIV0=0 means "DIV=0 => freq/(0+1)=freq"
		// SEL=0 might correspond to "external" or might be the main clock,
		// depending on how your MCU's clock tree is defined. The manual's example
		// is picking the external clock at 12 MHz, so we do:
		this.SYSTICKCLKDIV0 = 0;
		this.SYSTICKCLKSEL0 = 0; // 0 => external clock, if that's how your device enumerates it
		this.SYSTICKCLKDIV1 = 0;
		this.SYSTICKCLKSEL1 = 0;
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

	public int getSYSTICKCLKDIV0() {
		return SYSTICKCLKDIV0;
	}

	public void setSYSTICKCLKDIV0(int sYSTICKCLKDIV0) {
		SYSTICKCLKDIV0 = sYSTICKCLKDIV0;
	}

	public int getSYSTICKCLKSEL0() {
		return SYSTICKCLKSEL0;
	}

	public void setSYSTICKCLKSEL0(int sYSTICKCLKSEL0) {
		SYSTICKCLKSEL0 = sYSTICKCLKSEL0;
	}

	public int getSYSTICKCLKDIV1() {
		return SYSTICKCLKDIV1;
	}

	public void setSYSTICKCLKDIV1(int sYSTICKCLKDIV1) {
		SYSTICKCLKDIV1 = sYSTICKCLKDIV1;
	}

	public int getSYSTICKCLKSEL1() {
		return SYSTICKCLKSEL1;
	}

	public void setSYSTICKCLKSEL1(int sYSTICKCLKSEL1) {
		SYSTICKCLKSEL1 = sYSTICKCLKSEL1;
	}

}
