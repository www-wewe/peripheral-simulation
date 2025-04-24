/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.ui;

/**
 * Enum representing the choices for the simulation GUI.
 * <p>
 * This enum provides a way to specify the type of GUI to be used for the
 * simulation, either a table or a graph.
 * </p>
 * 
 * @author Veronika Lenková
 */
public enum SimulationGuiChoice {

	/** The table GUI choice */
	TABLE("Table"),
	/** The graph GUI choice */
	GRAPH("Graph");

	/** The display name of the GUI choice */
	private final String displayName;

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

	/**
	 * Constructor for SimulationGuiChoice enum.
	 *
	 * @param displayName the display name of the GUI choice
	 */
	SimulationGuiChoice(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}

}
