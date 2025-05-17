/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.model.FlexIOModel;
import peripheralsimulation.model.flexio.FlexIOConfig;
import peripheralsimulation.utils.RegisterMap;

/**
 * Test class for {@link FlexIOModel}.
 *
 * <p>
 * Tests cover two essential behaviours:
 * </p>
 * <ul>
 * <li><b>PWM generation</b> – verifies that a timer configured for dual-8-bit
 * PWM (TIMOD = 10) really toggles its output on every timer tick when both low-
 * and high-reload values equal&nbsp;1.</li>
 * <li><b>Timer status flag</b> – verifies that a timer in dual-8-bit baud/bit
 * mode (TIMOD = 01) sets the corresponding TIMSTAT bit after the programmed
 * number of bits has elapsed.</li>
 * </ul>
 *
 * <p>
 * All tests use a real {@link SimulationEngine} instance and the concrete
 * FlexIO implementation.
 * </p>
 *
 * @author Veronika Lenková
 */
public class FlexIOModelTest {

	/*
	 * Creates a minimal RegisterMap containing exactly one timer and zero shifters.
	 */
	private static RegisterMap createRegisterMap(int timctl0, int timcfg0, int timcmp0) {
		Map<Integer, Integer> regs = new HashMap<>();

		/* Enable the peripheral */
		regs.put(FlexIOConfig.CTRL_OFFSET, 0x0000_0001);

		/* Single-timer configuration (Timer 0) */
		regs.put(FlexIOConfig.TIMCTL0_OFFSET, timctl0);
		regs.put(FlexIOConfig.TIMCFG0_OFFSET, timcfg0);
		regs.put(FlexIOConfig.TIMCMP0_OFFSET, timcmp0);

		/* The presence of TIMCTL0 is enough for FlexIOConfig to detect 1 timer */
		return new RegisterMap(regs);
	}

	/**
	 * Timer in PWM mode (TIMOD = 10) should alternate its output each tick when
	 * both the low- and high-reload counts are 1.
	 */
	@Test
	public void testPwmToggle() {
		/* TIMCTL0: TIMOD = 10b (bits 1-0) */
		int timctl0 = 0b10; // other bits 0 → pin active-high, ext. trigger
		/* TIMCFG0: use default 0 – initial output = HIGH (timerOutput = 00) */
		int timcfg0 = 0;
		/* TIMCMP0: low = 0, high = 0 → reloads 1 tick / 1 tick */
		int timcmp0 = 0x0000;

		RegisterMap map = createRegisterMap(timctl0, timcfg0, timcmp0);
		FlexIOConfig cfg = new FlexIOConfig(map);
		FlexIOModel model = new FlexIOModel(cfg);

		SimulationEngine engine = new SimulationEngine((t, outs) -> {
		});
		engine.initSimulation();
		model.initialize(engine);

		/* Initial state – HIGH */
		assertEquals("PWM starts HIGH", 1, (int) model.getOutputs()[0]);

		/* After first tick – LOW */
		model.update(engine);
		assertEquals("First toggle to LOW", 0, (int) model.getOutputs()[0]);

		/* After second tick – HIGH */
		model.update(engine);
		assertEquals("Second toggle back to HIGH", 1, (int) model.getOutputs()[0]);
	}

	/**
	 * Timer in dual-8-bit baud/bit mode (TIMOD = 01) must set TIMSTAT after the
	 * programmed number of bits has been transmitted.
	 *
	 * <p>
	 * Configuration: divider reload = 2 ticks, bit-counter reload = 1 → the status
	 * flag should rise after exactly two engine updates.
	 * </p>
	 */
	@Test
	public void testTimStatSetInBaudBitMode() {
		/* TIMCTL0: TIMOD = 01b */
		int timctl0 = 0b01;
		/* TIMCFG0: default 0 (initial HIGH) */
		int timcfg0 = 0;
		/*
		 * TIMCMP0: low byte = 0 → lowReload = (0+1)*2 = 2 ticks high byte = 0 →
		 * highReload = (0+1) = 1 bit
		 */
		int timcmp0 = 0x0000;

		RegisterMap map = createRegisterMap(timctl0, timcfg0, timcmp0);
		FlexIOConfig cfg = new FlexIOConfig(map);
		FlexIOModel model = new FlexIOModel(cfg);

		SimulationEngine engine = new SimulationEngine((t, outs) -> {
		});
		engine.initSimulation();
		model.initialize(engine);

		/* TIMSTAT must be clear right after reset */
		assertEquals("TIMSTAT clear after reset", 0, cfg.getTimStat());

		/* Two updates → one full “bit-time” elapsed, TIMSTAT[0] must be 1 */
		model.update(engine);
		model.update(engine);
		assertTrue("TIMSTAT[0] set after one bit period", (cfg.getTimStat() & 0x1) != 0);
	}

}
