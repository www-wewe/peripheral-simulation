/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.engine.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

import org.junit.Before;
import org.junit.Test;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.engine.UserEvent;
import peripheralsimulation.engine.UserEventGenerator;
import peripheralsimulation.engine.UserEventType;
import peripheralsimulation.model.Peripheral;
import peripheralsimulation.model.PeripheralModel;

/**
 * Test class for the UserEventGenerator.
 * 
 * @author Veronika Lenková
 */
public class UserEventGeneratorTest {

	/** Minimal task holder used by {@link FakeSimulationEngine}. */
	private static class ScheduledTask {
		final double time;
		final Runnable action;

		ScheduledTask(double time, Runnable action) {
			this.time = time;
			this.action = action;
		}
	}

	/**
	 * A very small stand-in for the real simulation kernel. It merely collects the
	 * (time, runnable) pairs and lets the test trigger them in chronological order
	 * so that we can observe real execution effects.
	 */
	private static class FakeSimulationEngine extends SimulationEngine {
		public FakeSimulationEngine(BiConsumer<Double, Object[]> outputHandler) {
			super(outputHandler);
		}

		private final List<ScheduledTask> taskQueue = new ArrayList<>();

		public void scheduleEvent(double time, Runnable action) {
			taskQueue.add(new ScheduledTask(time, action));
		}

		/** Executes the earliest task (if any). */
		void runNext() {
			taskQueue.sort(Comparator.comparingDouble(t -> t.time));
			ScheduledTask next = taskQueue.remove(0);
			next.action.run();
		}

		/** Runs up to <code>n</code> tasks (or fewer if the queue empties). */
		void runFirstN(int n) {
			for (int i = 0; i < n && !taskQueue.isEmpty(); i++) {
				runNext();
			}
		}

		int size() {
			return taskQueue.size();
		}
	}

	/**
	 * A counting peripheral that records how often {@link #applyUserEvent} gets
	 * called. Only the single method exercised by the SUT is overridden; any
	 * additional behaviour from the real
	 * {@link peripheralsimulation.model.PeripheralModel} is not required for these
	 * unit tests.
	 */
	private static class CountingPeripheral implements PeripheralModel {
		private int callCount = 0;

		public void applyUserEvent(UserEvent event) {
			callCount++;
		}

		int getCount() {
			return callCount;
		}

		@Override
		public void initialize(SimulationEngine engine) {
			// Intentionally empty

		}

		@Override
		public void update(SimulationEngine engine) {
			// Intentionally empty

		}

		@Override
		public String getOutputName(int index) {
			return null;
		}

		@Override
		public int getOutputIndex(String name) {
			return 0;
		}

		@Override
		public Object[] getOutputs() {
			return null;
		}

		@Override
		public String[] getOutputNames() {
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
	}

	/* ------------------------------------------------------------------ */
	/* Test setup */
	/* ------------------------------------------------------------------ */

	private FakeSimulationEngine engine;
	private CountingPeripheral peripheral;

	@Before
	public void setUp() {
		engine = new FakeSimulationEngine((d, o) -> {
		});
		peripheral = new CountingPeripheral();
	}

	/* ------------------------------------------------------------------ */
	/* Test cases */
	/* ------------------------------------------------------------------ */

	@Test
	public void schedulesOneTimeEvent() {
		UserEventGenerator gen = new UserEventGenerator();
		UserEvent event = new UserEvent(5.0, /* period */ 0.0, /* repeat */ 0, peripheral, UserEventType.TOGGLE_BIT,
				0x10, 3, 0);

		gen.addEvent(event);
		gen.scheduleAll(engine);

		assertEquals("Exactly one task should have been scheduled", 1, engine.size());

		ScheduledTask task = engine.taskQueue.get(0);
		assertEquals(5.0, task.time, 1e-6);

		engine.runNext();

		assertEquals(1, peripheral.getCount());
		assertEquals(0, engine.size());
	}

	@Test
	public void schedulesFiniteRepeatingEvent() {
		int repeatCount = 2; // repeats *after* first firing
		double period = 1.5;
		double start = 0.5;

		UserEvent event = new UserEvent(start, period, repeatCount, peripheral, UserEventType.TOGGLE_BIT, 0x20, 1, 0);

		UserEventGenerator gen = new UserEventGenerator();
		gen.addEvent(event);
		gen.scheduleAll(engine);

		engine.runFirstN(repeatCount + 1); // first + repeats

		assertEquals(repeatCount + 1, peripheral.getCount());
	}

	@Test
	public void schedulesInfiniteRepeatingEvent_firstFewFirings() {
		int firesObserved = 5;

		UserEvent event = new UserEvent(0.0, 0.25, /* repeat≤0→infinite */ 0, peripheral, UserEventType.TOGGLE_BIT,
				0x30, 0, 0);

		UserEventGenerator gen = new UserEventGenerator();
		gen.addEvent(event);
		gen.scheduleAll(engine);

		engine.runFirstN(firesObserved);

		assertEquals(firesObserved, peripheral.getCount());
	}

	@Test
	public void clearEventsPreventsScheduling() {
		UserEventGenerator gen = new UserEventGenerator();
		gen.addEvent(new UserEvent(1.0, 0, 0, peripheral, UserEventType.TOGGLE_BIT, 0x01, 0, 0));

		gen.clearEvents();
		gen.scheduleAll(engine);

		assertEquals(0, engine.size());
		assertEquals(0, peripheral.getCount());
	}
}
