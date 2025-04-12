package peripheralsimulation.model.systick;

import peripheralsimulation.utils.RegisterUtils;

public class SysTickTimerConfig {

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

	public SysTickTimerConfig(SysTickRegisterDump dump) {
		currentValue = dump.getSYST_CVR();

		// 1) SYST_CSR bits
		setSYST_CSR(dump.getSYST_CSR());

		// 2) Reload value (24-bit)
		setSYST_RVR(dump.getSYST_RVR());

		// 3) SYST_CALIB
		setSYST_CALIB(dump.getSYST_CALIB());

		// 4) Decide final clock freq from MCUXpresso
		// e.g. if useCpuClock => pick mainClk
		// else => pick externalClk
		// clock settings from SYSTICKCLKDIVn, SYSTICKCLKSELn ???
		double freq = 0.0;

		if (useCpuClock) {
			freq = dump.getMainClk();
		} else {
			freq = dump.getExternalClk();
		}

		// store in config
		cpuClockFreq = useCpuClock ? freq : 0;
		externalFreq = useCpuClock ? 0 : freq;
	}

	public int getSYST_CSR() {
		int value = 0;
		value |= (enable ? 1 : 0) << 0; // ENABLE
		value |= (tickInt ? 1 : 0) << 1; // TICKINT
		value |= (useCpuClock ? 1 : 0) << 2; // CLKSOURCE
		return value;
	}
	
	public void setSYST_CSR(int value) {
		enable = ((value >> 0) & 1) == 1;
		tickInt = ((value >> 1) & 1) == 1;
		useCpuClock = ((value >> 2) & 1) == 1;
	}

	public int getSYST_RVR() {
		return reloadValue & RegisterUtils.BIT_MASK;
	}

	public void setSYST_RVR(int value) {
		reloadValue = value & RegisterUtils.BIT_MASK;
	}

	public int getSYST_CVR() {
		return currentValue & RegisterUtils.BIT_MASK;
	}

	public void setSYST_CVR(int value) {
		currentValue = 0;
		countFlag = false;
	}

	public int getSYST_CALIB() {
		int value = 0;
		value |= (tenms & RegisterUtils.BIT_MASK); // TENMS
		value |= (skew ? 1 : 0) << 30; // SKEW
		value |= (noRef ? 1 : 0) << 31; // NOREF
		return value;
	}

	public void setSYST_CALIB(int value) {
		tenms = value & RegisterUtils.BIT_MASK;
		skew = ((value >> 30) & 1) == 1;
		noRef = ((value >> 31) & 1) == 1;
	}

}
