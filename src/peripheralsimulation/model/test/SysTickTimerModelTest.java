/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.model.SysTickTimerModel;
import peripheralsimulation.model.systick.SysTickTimerConfig;

/**
 * Test class for SysTickTimerModel.
 * 
 * This class contains unit tests for the SysTickTimerModel class, specifically
 * testing the underflow condition and the behavior of the CVR register.
 * 
 * @author Veronika Lenková
 */
public class SysTickTimerModelTest {

	/**
	 * Test if SysTickTimerModel correctly handles the underflow condition.
	 */
	@Test
	public void testUnderflow() {
		// Suppose freq=1.0 => each "tickPeriod=1.0"
		// reloadValue=3 => after 4 decrements we expect underflow
		SysTickTimerConfig config = new SysTickTimerConfig(0x3, // SYST_CSR
				3, // SYST_RVR
				0, // SYST_CVR
				0, // SYST_CALIB
				1, // mainClk
				0 // externalClk
		);
		SysTickTimerModel model = new SysTickTimerModel(config);

		SimulationEngine dummyEngine = new SimulationEngine((t, outs) -> {
		});
		dummyEngine.initSimulation();
		model.initialize(dummyEngine);

		// call update 4 times manually:
		for (int i = 0; i < 4; i++) {
			model.update(dummyEngine);
		}
		// Now we expect an underflow => countFlag is set => readCountFlag() => true
		assertTrue("Underflow sets the flag", model.readCountFlag());
		assertTrue("Interrupt is set", model.isInterruptGenerated());
		// current value should be 3 (reloadValue)
		assertEquals("Current value is 3", 3, model.readCVR());
	}

	/**
	 * Test if writing the CVR register sets the current value to 0 and clears the
	 * countflag.
	 */
	@Test
	public void testWriteCVR() {
		// test that writing CVR => sets currentValue=0, clears countFlag
		SysTickTimerConfig config = new SysTickTimerConfig(0x3, // SYST_CSR
				0x0001D4BF, // SYST_RVR
				0, // SYST_CVR
				0, // SYST_CALIB
				48e6, // mainClk
				12e6 // externalClk
		);
		SysTickTimerModel model = new SysTickTimerModel(config);

		model.writeCVR(123);
		assertEquals(0, model.readCVR());
		assertFalse(model.readCountFlag());
	}
}
