package peripheralsimulation.ui;

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

	/**
	 * Lookup by display name.
	 *
	 * @param displayName the display name of the GUI choice
	 * @return the corresponding SimulationGuiChoice enum
	 * @throws IllegalArgumentException if no enum constant is found for the given
	 *                                  display name
	 */
	public static SimulationGuiChoice fromDisplayName(String displayName) {
		for (SimulationGuiChoice guiChoice : SimulationGuiChoice.values()) {
			if (guiChoice.displayName.equals(displayName)) {
				return guiChoice;
			}
		}
		throw new IllegalArgumentException("No enum constant for display name: " + displayName);
	}

}
