package peripheralsimulation.model;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import peripheralsimulation.engine.SimulationEngine;

public class TrueRandomNumberGeneratorModel implements PeripheralModel {

	private Random random;
	private int latestValue;

	public TrueRandomNumberGeneratorModel() {
		this.random = new Random(); // alebo fixný seed, napr. new Random(12345)
	}

	@Override
	public void initialize(SimulationEngine engine) {
		// každých 5 ms vygenerujeme novú hodnotu // alebo
		scheduleNextValue(engine, engine.getCurrentTime() + 0.005);
	}

	private void scheduleNextValue(SimulationEngine engine, double time) {
		engine.scheduleEvent(time, () -> update(engine));
	}

	@Override
	public void update(SimulationEngine engine) {
		// Vygenerujeme nový random
		latestValue = random.nextInt(256); // 8-bit random
		System.out.println("[TRNGModel] time=" + engine.getCurrentTime() + ", randomValue=" + latestValue);

		// Naplánujeme ďalšie generovanie
		scheduleNextValue(engine, engine.getCurrentTime() + 0.005);
	}

	@Override
	public Map<String, Object> getOutputValues() {
		return Map.of("RANDOM", latestValue);
	}

	@Override
	public Set<String> getOutputs() {
		return Set.of("RANDOM");
	}

}
