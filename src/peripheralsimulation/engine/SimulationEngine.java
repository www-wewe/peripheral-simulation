package peripheralsimulation.engine;

import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {
	private List<PeripheralModel> peripherals;
    private int currentTime;

    public SimulationEngine() {
        peripherals = new ArrayList<>();
        currentTime = 0;
    }

    public void addPeripheral(PeripheralModel peripheral) {
        peripherals.add(peripheral);
    }

    public void simulate(int steps) {
        for (int i = 0; i < steps; i++) {
            currentTime++;
            for (PeripheralModel peripheral : peripherals) {
                peripheral.update(currentTime);
            }
        }
    }

}
