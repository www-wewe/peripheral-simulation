package peripheralsimulation.engine;

/**
 * Represents a scheduled event in the simulation. Contains the time at which
 * the event occurs and the action to be performed.
 */
public class SimulationEvent {

	private final double time;
	private final Runnable runnable;

	public SimulationEvent(double time, Runnable runnable) {
		this.time = time;
		this.runnable = runnable;
	}

	public double time() {
		return time;
	}

	public void run() {
		runnable.run();
	}
}
