/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.model.Peripheral;
import peripheralsimulation.model.PeripheralModel;

/**
 * Test class for the SimulationEngine.
 * 
 * @author Veronika Lenková
 */
public class SimulationEngineTest {

	/**
	 * The SimulationEngine instance to be tested.
	 */
	private SimulationEngine engine;

	@Before
	public void setUp() {
		engine = new SimulationEngine((time, outputs) -> {
		});
		engine.initSimulation();
	}

	/**
	 * Verifies that the engine terminates immediately when no events are scheduled
	 * and keeps the currentTime at 0.
	 */
	@Test
	public void testNoEventsSimulationDoesNotRun() {
		engine.startSimulation(5);
		assertFalse("Simulation should stop when there are no events", engine.isSimulationRunning());
		assertEquals("Time should remain at 0 when no events are processed", 0.0, engine.getCurrentTime(), 0.0);
	}

	/**
	 * Two events scheduled for the exact same time must both execute.
	 */
	@Test
	public void testSimultaneousEvents() {
		AtomicInteger counter = new AtomicInteger();
		engine.scheduleEvent(2.0, counter::incrementAndGet);
		engine.scheduleEvent(2.0, counter::incrementAndGet);

		engine.startSimulation(3);
		assertEquals("Both events scheduled at t=2.0 should fire", 2, counter.get());
	}

	/**
	 * Confirms that peripherals are initialised and their outputs queried while the
	 * simulation is running.
	 */
	@Test
	public void testPeripheralOutputHandling() {
		AtomicInteger initCalls = new AtomicInteger();
		AtomicInteger outputCalls = new AtomicInteger();

		PeripheralModel dummyPeripheral = new PeripheralModel() {
			@Override
			public void initialize(SimulationEngine engine) {
				initCalls.incrementAndGet();
				// Schedule at least one event so the simulation actually advances.
				engine.scheduleEvent(1.0, () -> {
				});
			}

			@Override
			public Object[] getOutputs() {
				outputCalls.incrementAndGet();
				return new Object[] { "ok" };
			}

			@Override
			public void update(SimulationEngine engine) {
				// Intentionally empty

			}

			@Override
			public String getOutputName(int index) {
				// Intentionally empty
				return null;
			}

			@Override
			public int getOutputIndex(String name) {
				return 0;
			}

			@Override
			public String[] getOutputNames() {
				// Intentionally empty
				return null;
			}

			@Override
			public void setRegisterValue(int registerAddress, int value) {
				// Intentionally empty

			}

			@Override
			public Integer getRegisterValue(int registerAddress) {
				return null;
			}

			@Override
			public Peripheral getPeripheralType() {
				return null;
			}
		};

		engine.setPeripheralModel(dummyPeripheral);
		engine.initSimulation();
		engine.startSimulation(2);

		assertEquals("Peripheral initialize() should be called once", 1, initCalls.get());
		assertTrue("Peripheral getOutputs() should be called at least once", outputCalls.get() > 0);
	}

	/**
	 * After stopSimulation() the engine must reset its state.
	 */
	@Test
	public void testStopSimulationResetsState() {
		engine.scheduleEvent(1.0, () -> {
		});
		engine.startSimulation(1);
		engine.stopSimulation();

		assertFalse("Engine must not report running after stop", engine.isSimulationRunning());
		assertEquals("Current time should reset to 0 after stop", 0.0, engine.getCurrentTime(), 0.0);
	}

	/**
	 * cleanSimulation() should remove all peripherals without throwing exceptions
	 * on re‑initialisation.
	 */
	@Test
	public void testCleanSimulationClearsModels() {
		// Minimal stub peripheral
		engine.setPeripheralModel(new PeripheralModel() {
			@Override
			public void initialize(SimulationEngine e) {
			}

			@Override
			public Object[] getOutputs() {
				return new Object[0];
			}

			@Override
			public void update(SimulationEngine engine) {
				// Intentionally empty

			}

			@Override
			public String getOutputName(int index) {
				// Intentionally empty
				return null;
			}

			@Override
			public int getOutputIndex(String name) {
				return 0;
			}

			@Override
			public String[] getOutputNames() {
				// Intentionally empty
				return null;
			}

			@Override
			public void setRegisterValue(int registerAddress, int value) {
				// Intentionally empty

			}

			@Override
			public Integer getRegisterValue(int registerAddress) {
				return null;
			}

			@Override
			public Peripheral getPeripheralType() {
				return null;
			}
		});

		engine.cleanSimulation();
		// Should not throw
		engine.initSimulation();
	}

	/**
	 * Test if the simulation engine correctly handles the scheduling of events.
	 */
	@Test
	public void testEventOrdering1() {
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
		// schedule event beyond maxTime
		final boolean[] called = { false };
		engine.scheduleEvent(10.0, () -> called[0] = true);

		engine.startSimulation(5.0);
		assertFalse("Event at t=10.0 must not fire if maxTime=5.0", called[0]);
	}
}
