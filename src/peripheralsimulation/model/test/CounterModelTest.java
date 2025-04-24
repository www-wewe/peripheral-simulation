/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.model.CounterModel;

/**
 * Test class for the CounterModel.
 *
 * @author Veronika Lenková
 */
public class CounterModelTest {

	/** Holder for one scheduled event. */
	private static class Task {
		final double time;
		final Runnable action;

		Task(double time, Runnable action) {
			this.time = time;
			this.action = action;
		}
	}

	/**
	 * Minimal in‑memory stand‑in for the real simulation kernel. It supports the
	 * subset of functionality required by {@link CounterModel}: keeping a notion of
	 * *current* simulation time, accepting scheduled callbacks, and exposing a
	 * *running* flag so the model knows whether to keep rescheduling itself.
	 */
	private static class FakeSimulationEngine extends SimulationEngine {
		public FakeSimulationEngine() {
			super(null);
		}

		private double currentTime = 0.0;
		private final List<Task> queue = new ArrayList<>();
		private boolean running = true;

		public void scheduleEvent(double time, Runnable action) {
			queue.add(new Task(time, action));
		}

		public double getCurrentTime() {
			return currentTime;
		}

		public boolean isSimulationRunning() {
			return running;
		}

		/** Executes the earliest pending task (if any). */
		void runNext() {
			queue.sort(Comparator.comparingDouble(t -> t.time));
			Task next = queue.remove(0);
			// advance simulation time *before* running the action
			currentTime = next.time;
			next.action.run();
		}

		/** Executes up to {@code n} tasks or until the queue empties. */
		void runFirstN(int n) {
			for (int i = 0; i < n && !queue.isEmpty(); i++) {
				runNext();
			}
		}

		/** Stops the simulation so models stop self‑scheduling. */
		void stop() {
			running = false;
		}

		int queuedTasks() {
			return queue.size();
		}
	}

	/** Simulation engine for the tests. */
	private FakeSimulationEngine engine;
	/** Counter model under test. */
	private CounterModel counter;

	/** 8‑bit counter ticking at 1 kHz, prescaler 1 → tickPeriod = 1 ms. */
	@Before
	public void setUp() {
		engine = new FakeSimulationEngine();
		counter = new CounterModel(255, /* initial */ 0, /* clk Hz */ 1000.0, /* presc */ 1);
		counter.initialize(engine);
	}

	@Test
	public void firstIncrementScheduledCorrectly() {
		// After initialise() exactly one task must have been scheduled at t = 0.001
		assertEquals("One task expected immediately after initialise", 1, engine.queuedTasks());
		assertEquals(0.001, engine.queue.get(0).time, 1e-9);

		// Execute it and verify internal state.
		engine.runNext();
		Object[] outputs = counter.getOutputs();

		assertEquals(1, outputs[0]); // currentValue
		assertEquals(Boolean.FALSE, outputs[1]); // overflow flag
	}

	@Test
	public void counterOverflowsAndSetsFlag() {
		engine = new FakeSimulationEngine();
		// Configure a very small counter so that we can reach overflow quickly.
		counter = new CounterModel(3, /* start */ 2, /* clk */ 1.0, /* presc */ 1); // tickPeriod = 1 s for clarity
		counter.initialize(engine);

		// Two updates: 2→3 (no overflow), then 3→0 (overflow)
		engine.runFirstN(2);

		Object[] outputs = counter.getOutputs();
		assertEquals(0, outputs[0]); // Wrapped to zero
		assertEquals(Boolean.TRUE, outputs[1]); // Overflow flag set
	}

	@Test
	public void modelStopsReschedulingWhenSimulationStops() {
		// Run a handful of increments, then halt the simulation and ensure no
		// new tasks get queued after the final update.
		engine.runFirstN(3);
		engine.stop(); // Fake an external request to stop simulation

		int tasksBefore = engine.queuedTasks();
		// Execute whatever was already queued at the moment of stopping
		engine.runFirstN(tasksBefore);

		int tasksAfter = engine.queuedTasks();
		assertEquals("Model must *not* schedule new tasks once engine is stopped", 0, tasksAfter);
	}

	@Test
	public void outputNameAndIndexMappingsAreConsistent() {
		assertArrayEquals(new String[] { "CURRENT_VALUE", "OVERFLOW_OCCURRED" }, counter.getOutputNames());
		assertEquals(0, counter.getOutputIndex("CURRENT_VALUE"));
		assertEquals(1, counter.getOutputIndex("OVERFLOW_OCCURRED"));
	}

}
