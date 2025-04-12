package peripheralsimulation.model;

public enum Peripheral {

	SYSTICKTIMER("System Tick Timer"),
	COUNTER("Counter");

	private final String displayName;

	Peripheral(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}

}
