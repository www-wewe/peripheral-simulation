/** Copyright (c) 2025, Veronika Lenková */
package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import peripheralsimulation.engine.SimulationEngine;

/**
 * Test class for the SimulationEngine.
 * 
 * @author Veronika Lenková
 */
public class SimulationEngineTest {

	/**
	 * Test if the simulation engine correctly handles the scheduling of events.
	 */
	@Test
	public void testEventOrdering1() {
		SimulationEngine engine = new SimulationEngine((time, outputs) -> {
		});
		engine.initSimulation();

		int[] calls = new int[2];
		engine.scheduleEvent(3.0, () -> calls[0] = 3);
		engine.scheduleEvent(1.0, () -> calls[1] = 1);

		engine.startSimulation(1);
		assertEquals("Event at t=1.0 fired", 0, calls[0]);
		assertEquals("Event at t=3.0 fired", 1, calls[1]);
	}

	/**
	 * Test if the simulation engine correctly handles the scheduling of events.
	 */
	@Test
	public void testEventOrdering2() {
		SimulationEngine engine = new SimulationEngine((time, outputs) -> {
		});
		engine.initSimulation();

		int[] calls = new int[2];
		engine.scheduleEvent(3.0, () -> calls[0] = 3);
		engine.scheduleEvent(1.0, () -> calls[1] = 1);

		engine.startSimulation(3);
		assertEquals("Event at t=1.0 fired", 3, calls[0]);
		assertEquals("Event at t=3.0 fired", 1, calls[1]);
	}

	/**
	 * Test situation when the event is shedueled after max simulation time.
	 */
	@Test
	public void testMaxTimeExceeded() {
		SimulationEngine engine = new SimulationEngine((time, outputs) -> {
		});
		engine.initSimulation();

		// schedule event beyond maxTime
		final boolean[] called = { false };
		engine.scheduleEvent(10.0, () -> called[0] = true);

		engine.startSimulation(5.0);
		assertFalse("Event at t=10.0 must not fire if maxTime=5.0", called[0]);
	}
}
