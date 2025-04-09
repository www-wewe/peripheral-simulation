package peripheralsimulation.model;

import java.util.Random;
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
	public int getOutputCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getOutputName(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getOutputNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getOutputs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getOutputIndex(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

//	@Override
//	public Map<String, Object> getOutputValues() {
//		return Map.of("RANDOM", latestValue);
//	}
//
//	@Override
//	public Set<String> getOutputs() {
//		return Set.of("RANDOM");
//	}

}
