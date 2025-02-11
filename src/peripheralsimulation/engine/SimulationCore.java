package peripheralsimulation.engine;

import java.util.function.BiConsumer;
import model.modeling.digraph;
import model.simulation.coordinator;
import peripheralsimulation.PeripheralSimulation;

/**
 * Universal simulation core, which is able to simulate any peripheral model.
 */
public class SimulationCore {

	private coordinator simulator;
	private boolean running;
	private boolean paused;
	private int start;
	private BiConsumer<Double, String> outputHandler;

	public SimulationCore(PeripheralSimulation simulation, BiConsumer<Double, String> outputHandler) {
		if (!(simulation instanceof digraph s)) {
            throw new IllegalArgumentException("Model musí byť typu diagraph.");
        }
		this.simulator = new coordinator(s);
		this.outputHandler = outputHandler;
		this.running = false;
		this.paused = false;
		this.start = 0;
	}

	public void startSimulation(int steps) {
		running = true;
		paused = false;
		if (start == 0) {
			simulator.initialize();
		}
		for (int i = start; i < steps && running; i++) {
			if (paused) {
				start = i;
				System.out.println("[SimulationCore] Simulácia pozastavená.");
				break;
			}
			double currentTime = simulator.getTN();
			System.out.println("[SimulationCore] Iterácia " + i + ", čas: " + currentTime);
			String outputMessage = getOutput();
			System.out.println("[SimulationCore] Čas: " + currentTime + ", Výstup: " + outputMessage);

			if (currentTime == Double.POSITIVE_INFINITY) {
				System.out.println("[SimulationCore] Simulácia ukončená, žiadne ďalšie udalosti.");
				break;
			}

			simulator.simulate(1);

			// Poslanie výstupu do SimulationView
			if (outputHandler != null) {
				outputHandler.accept(currentTime, outputMessage);
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void pauseSimulation() {
		paused = true;
	}

	public void stopSimulation() {
		running = false;
		start = 0;
		System.out.println("[SimulationCore] Simulácia zastavená.");
	}

	public double getCurrentTime() {
		return simulator.getTN();
	}

	public String getOutput() {
		String output = simulator.getOutputForTimeView().toString();
		System.out.println("[SimulationCore] Výstup: " + output);
		return output.isEmpty() ? "Žiadny výstup" : output;
	}
	
	public boolean isSimulationRunning() {
		return running;
	}

	public boolean isSimulationPaused() {
		return paused;
	}

}
