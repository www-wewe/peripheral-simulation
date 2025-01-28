package peripheralsimulation;

import model.simulation.*;
import GenCol.*;

public class SimulationRunner {
    public static void main(String[] args) {
        // Inicializujte coupled model
        PeripheralSimulation model = new PeripheralSimulation();

        // Inicializujte simulátor
        coordinator simulator = new coordinator(model);

        // Spustite simuláciu
        simulator.initialize();
        simulator.simulate(50); // Simulujte 50 krokov
    }
}