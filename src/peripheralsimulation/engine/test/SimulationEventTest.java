/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import peripheralsimulation.engine.SimulationEvent;

/**
 * Test class for the SimulationEvent class.
 *
 * @author Veronika Lenková
 */
public class SimulationEventTest {

	/**
	 * {@link SimulationEvent#getTime()} should return the exact scheduled
	 * timestamp.
	 */
	@Test
	public void testGetTimeReturnsScheduledTime() {
		double scheduled = 4.2;
		SimulationEvent event = new SimulationEvent(scheduled, () -> {
		});
		assertEquals("getTime should return the scheduled time", scheduled, event.getTime(), 0.0);
	}

	/**
	 * {@link SimulationEvent#run()} must execute the provided runnable.
	 */
	@Test
	public void testRunInvokesRunnable() {
		AtomicBoolean executed = new AtomicBoolean(false);
		SimulationEvent event = new SimulationEvent(1.0, () -> executed.set(true));
		event.run();
		assertTrue("run() should execute the runnable", executed.get());
	}

	/**
	 * The event must forward any exception thrown by the runnable to the caller,
	 * allowing calling code to handle it.
	 */
	@Test(expected = RuntimeException.class)
	public void testRunPropagatesException() {
		SimulationEvent event = new SimulationEvent(0.0, () -> {
			throw new RuntimeException("boom");
		});
		event.run(); // Exception expected – the annotation handles the assertion.
	}

	/**
	 * Demonstrates that using a priority queue with a time‑based comparator
	 * enqueues events in ascending order of their timestamp.
	 */
	@Test
	public void testPriorityQueueOrderingByTime() {
		SimulationEvent late = new SimulationEvent(5.0, () -> {
		});
		SimulationEvent early = new SimulationEvent(1.0, () -> {
		});
		PriorityQueue<SimulationEvent> queue = new PriorityQueue<>(
				Comparator.comparingDouble(SimulationEvent::getTime));

		queue.add(late);
		queue.add(early);

		assertSame("Queue should return the earliest event first", early, queue.poll());
		assertSame("Queue should return the later event next", late, queue.poll());
	}

}
