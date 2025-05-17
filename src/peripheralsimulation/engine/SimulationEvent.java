/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.engine;

/**
 * Represents a scheduled event in the simulation. Contains the time at which
 * the event occurs and the action to be performed.
 * 
 * @author Veronika Lenková
 */
public class SimulationEvent {

	/** The time at which the event occurs. */
	private final double time;

	/** The action to be performed when the event occurs. */
	private final Runnable runnable;

	/**
	 * Constructs a new SimulationEvent with the specified time and action.
	 * 
	 * @param time     The time at which the event occurs.
	 * @param runnable The action to be performed when the event occurs.
	 */
	public SimulationEvent(double time, Runnable runnable) {
		this.time = time;
		this.runnable = runnable;
	}

	/**
	 * Returns the time at which the event occurs.
	 * 
	 * @return The time at which the event occurs.
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Executes the action associated with this event.
	 */
	public void run() {
		runnable.run();
	}
}
