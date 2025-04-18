package peripheralsimulation.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.BiConsumer;

import peripheralsimulation.io.UserPreferences;
import peripheralsimulation.model.PeripheralModel;

/**
 * Universal simulation core, which is able to simulate any peripheral model.
 */
public class SimulationEngine {

	/**
	 * A priority queue to hold all scheduled events. The queue is ordered by
	 * ascending event time.
	 */
	private final Queue<SimulationEvent> eventQueue = new PriorityQueue<>(
			Comparator.comparingDouble(SimulationEvent::time));
	/**
	 * Current simulation time in arbitrary time units.
	 */
	private double currentTime;

	/**
	 * Flag indicating whether the simulation is running.
	 */
	private boolean running;

	/**
	 * List of simulation models (peripherals, generators) managed by this
	 * simulation engine.
	 */
	private final List<PeripheralModel> models = new ArrayList<>();

	/**
	 * A consumer to handle simulation output (e.g., display in a view). Key:
	 * current time, Value: map of peripheral outputs with their values.
	 */
	private BiConsumer<Double, Object[]> outputHandler;

	/**
	 * User preferences for the simulation.
	 */
	private UserPreferences userPreferences = UserPreferences.getInstance();

	/**
	 * The time at which the next output should be produced.
	 */
	private double nextMonitorTime = 0.0;

	/**
	 * The user event generator responsible for scheduling user events.
	 */
	private UserEventGenerator userEventGenerator = new UserEventGenerator();

	/**
	 * Constructor for the simulation engine.
	 * 
	 * @param outputHandler A consumer to handle simulation output.
	 */
	public SimulationEngine(BiConsumer<Double, Object[]> outputHandler) {
		this.outputHandler = outputHandler;
		this.currentTime = 0.0;
		this.running = false;
	}

	/**
	 * Initializes or resets the simulation. Clears the event queue and sets time to
	 * zero.
	 */
	public void initSimulation() {
		eventQueue.clear();
		currentTime = 0.0;
		running = false;

		// Let each peripheral initialize itself.
		for (PeripheralModel peripheralModel : models) {
			peripheralModel.initialize(this);
		}

		userEventGenerator.scheduleAll(this);
	}

	/**
	 * Starts or continues the simulation up to a specified maximum time (or until
	 * no more events).
	 * 
	 * @param maxTime The simulation will not proceed beyond this time.
	 */
	public void startSimulation(double maxTime) {
		running = true;
		nextMonitorTime = userPreferences.getSimulationTimeRangeFrom();
		double period = userPreferences.getMonitoringPeriod();

		while (running && !eventQueue.isEmpty()) {
			SimulationEvent next = eventQueue.peek();
			if (next.time() > maxTime) {
				break;
			}

			// Pop the event from the queue
			eventQueue.poll();

			// Advance simulation time
			currentTime = next.time();

			// Execute event logic
			next.run();

			for (PeripheralModel model : models) {
				Object[] outputs = model.getOutputs();
				if (currentTime >= nextMonitorTime && outputHandler != null) {
					// Poslanie výstupu do SimulationView
					outputHandler.accept(currentTime, outputs);
					if (period > 0) {
						nextMonitorTime += period;
					}
				}
			}
			try {
				Thread.sleep(userPreferences.getMillisToWait());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		running = false;
		stopSimulation();
		System.out.println("[SimulationEngine] Simulácia ukončená, žiadne ďalšie udalosti.");
	}

	/**
	 * Schedules a new event in the simulation.
	 * 
	 * @param eventTime The time at which the event should trigger.
	 * @param action    The action (lambda or Runnable) to run at that time.
	 */
	public void scheduleEvent(double eventTime, Runnable action) {
		SimulationEvent event = new SimulationEvent(eventTime, action);
		eventQueue.add(event);
	}

	/**
	 * Adds a peripheral model to be managed by this engine.
	 * 
	 * @param peripheral The peripheral model implementing {@link PeripheralModel}.
	 */
	public void addPeripheral(PeripheralModel peripheral) {
		models.add(peripheral);
	}

	/**
	 * Adds a user event definition to the simulation. This allows for scheduling
	 * user-defined events that can be triggered at specific times.
	 * 
	 * @param event The user event definition to add.
	 */
	public void addUserEvent(UserEvent event) {
		userEventGenerator.addEvent(event);
	}

	public void stopSimulation() {
		running = false;
		eventQueue.clear();
		currentTime = 0.0;
		System.out.println("[SimulationEngine] Simulácia zastavená.");
	}

	public double getCurrentTime() {
		return currentTime;
	}

	public boolean isSimulationRunning() {
		return running;
	}

	public BiConsumer<Double, Object[]> getOutputHandler() {
		return outputHandler;
	}

	public void setOutputHandler(BiConsumer<Double, Object[]> outputHandler) {
		this.outputHandler = outputHandler;
	}

	/**
	 * Clears list of peripheral models and all scheduled user events.
	 */
	public void cleanSimulation() {
		models.clear();
		userEventGenerator.clearEvents();
	}

}
