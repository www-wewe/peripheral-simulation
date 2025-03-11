package peripheralsimulation.io.systick;

public class SysTickTimerConfig {

	private SysTickRegisterDump registers;

	// SYST_CVR
	public int currentValue = 0;

	// SYST_CSR bits
	public boolean enable; // ENABLE bit (CSR bit 0)
	public boolean tickInt; // TICKINT bit (CSR bit 1)
	public boolean useCpuClock; // CLKSOURCE bit (CSR bit 2)
	public boolean countFlag; // COUNTFLAG bit (CSR bit 16) (auto-clear on read or reload)

	// 24-bit reload from SYST_RVR
	public int reloadValue; // 0 to 0xFFFFFF

	// SYST_CALIB fields
	public int tenms; // TENMS from CALIB (bits [23:0])
	public boolean skew; // SKEW bit (bit 30)
	public boolean noRef; // NOREF bit (bit 31)

	// Final clock/prescaler for the simulation
	public double cpuClockFreq; // e.g. if useCpuClock = true
	public double externalFreq; // e.g. if useCpuClock = false
	public int prescalerDiv; // from SYSTICKCLKDIVn ?

	public SysTickTimerConfig(SysTickRegisterDump dump) {
		registers = dump;
		currentValue = dump.getSYST_CVR();

		// 1) SYST_CSR bits
		setSYST_CSR(registers.getSYST_CSR());

		// 2) Reload value (24-bit)
		setSYST_RVR(registers.getSYST_RVR());

		// 3) SYST_CALIB
		setSYST_CALIB(registers.getSYST_CALIB());

		// 4) Decide final clock freq from MCUXpresso
		// e.g. if useCpuClock => pick mainClk
		// else => pick externalClk
		// clock settings from SYSTICKCLKDIVn, SYSTICKCLKSELn ???
		double freq = 0.0;
		int prescaler = 1;

		if (useCpuClock) {
			freq = registers.getMainClk();
		} else {
			freq = registers.getExternalClk();
		}

		// Prescaler division from SYSTICKCLKDIVn ?
		prescaler = registers.getSYSTICKCLKDIV0();

		// store in config
		cpuClockFreq = useCpuClock ? freq : 0;
		externalFreq = useCpuClock ? 0 : freq;
		prescalerDiv = prescaler;
	}

	public void setSYST_CSR(int value) {
		enable = ((value >> 0) & 1) == 1;
		tickInt = ((value >> 1) & 1) == 1;
		useCpuClock = ((value >> 2) & 1) == 1;
	}

	public void setSYST_RVR(int value) {
		reloadValue = value & 0x00FFFFFF;
	}

	public void setSYST_CVR(int value) {
		currentValue = 0;
		countFlag = false;
	}

	public void setSYST_CALIB(int value) {
		tenms = value & 0x00FFFFFF;
		skew = ((value >> 30) & 1) == 1;
		noRef = ((value >> 31) & 1) == 1;
	}

}
