package peripheralsimulation.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
	private final List<PeripheralModel> modules = new ArrayList<>();

	/**
	 * A consumer to handle simulation output (e.g., display in a view).
	 * Key: current time, Value: map of peripheral outputs with their values.
	 */
	private BiConsumer<Double, Map<String, Object>> outputHandler;

	public SimulationEngine(BiConsumer<Double, Map<String, Object>> outputHandler) {
		this.outputHandler = outputHandler;
		this.currentTime = 0.0;
		this.running = false;
	}

	/**
	 * Initializes or resets the simulation.
	 * Clears the event queue and sets time to zero.
	 */
	public void initSimulation() {
		eventQueue.clear();
		currentTime = 0.0;
		running = false;

		// Let each peripheral initialize itself.
		for (PeripheralModel pm : modules) {
			pm.initialize(this);
		}
	}

	/**
	 * Starts or continues the simulation up to a specified maximum time (or until
	 * no more events).
	 * 
	 * @param maxTime The simulation will not proceed beyond this time.
	 */
	public void startSimulation(double maxTime) {
		running = true;
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

			// Poslanie výstupu do SimulationView
			for (PeripheralModel model : modules) {
				if (outputHandler != null) {
					Map<String, Object> outputs = model.getOutputValues();
					outputHandler.accept(getCurrentTime(), outputs);
				}
			}
			try {
				Thread.sleep(UserPreferences.getMillisToWait()); // TODO: parametrize
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		running = false;
		System.out.println("[SimulationCore] Simulácia ukončená, žiadne ďalšie udalosti.");
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
		modules.add(peripheral);
	}

	public void stopSimulation() {
		running = false;
		eventQueue.clear();
		currentTime = 0.0;
		System.out.println("[SimulationCore] Simulácia zastavená.");
	}

	public double getCurrentTime() {
		return currentTime;
	}

	public boolean isSimulationRunning() {
		return running;
	}

}
