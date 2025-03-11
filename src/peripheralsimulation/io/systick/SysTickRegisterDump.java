package peripheralsimulation.io.systick;

public class SysTickRegisterDump {
	// Example raw integers from the device
	private int SYST_CSR; // 0xE000E010
	private int SYST_RVR; // 0xE000E014
	private int SYST_CVR; // 0xE000E018 (if needed)
	private int SYST_CALIB; // 0xE000E01C

	// CPU frequency or external frequencies
	private double mainClk; // e.g. 48e6
	private double externalClk; // e.g. 12e6

	// MCUXpresso clock config
	// SYSCON->SYSTICKCLKDIV0,DIV,0, SYSCON->SYSTICKCLKSEL0,SEL,0 ...
	private int SYSTICKCLKDIV0; // e.g. 0
	private int SYSTICKCLKSEL0; // e.g. 0
	private int SYSTICKCLKDIV1; // e.g. 0
	private int SYSTICKCLKSEL1; // e.g. 7

	// example
	public SysTickRegisterDump() {
		this.SYST_CSR = 0xE000E010;
		this.SYST_RVR = 0xE000E014;
		this.SYST_CVR = 0xE000E018;
		this.SYST_CALIB = 0xE000E01C;
		this.mainClk = 48e6;
		this.externalClk = 12e6;
		this.SYSTICKCLKDIV0 = 0;
		this.SYSTICKCLKSEL0 = 0;
		this.SYSTICKCLKDIV1 = 0;
		this.SYSTICKCLKSEL1 = 7;
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
