package peripheralsimulation.views;

public enum SimulationGuiChoice {

	TABLE("Table"),
	GRAPH("Graph");
	
	private final String displayName;

	SimulationGuiChoice(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}

}
