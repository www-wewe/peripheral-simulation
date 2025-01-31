package peripheralsimulation.engine;

import java.util.function.BiConsumer;
import model.modeling.digraph;
import model.simulation.coordinator;

/**
 * Universal simulation core, which is able to simulate any peripheral model.
 */
public class SimulationCore {

    private coordinator simulator;
    private boolean running;
    private BiConsumer<Double, String> outputHandler;

    public SimulationCore(digraph model, BiConsumer<Double, String> outputHandler) {
        this.simulator = new coordinator(model);
        this.running = false;
        this.outputHandler = outputHandler;
    }

    public void startSimulation(int steps) {
        simulator.initialize();
        running = true;

        for (int i = 0; i < steps && running; i++) {
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
        }
    }

    public void stopSimulation() {
        running = false;
    }

    public double getCurrentTime() {
        return simulator.getTN();
    }

    public String getOutput() {
        String output = simulator.getOutputForTimeView().toString();
        System.out.println("[SimulationCore] Výstup: " + output);
        return output.isEmpty() ? "Žiadny výstup" : output; 
    }

}

