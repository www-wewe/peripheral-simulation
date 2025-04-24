/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.io;

/**
 * This interface defines the methods that should be implemented by classes
 * interested in receiving updates about changes in user preferences
 * (SimulationView). Implementing classes should define the actions to take when
 * the selected outputs or simulation GUI changes.
 *
 * @author Veronika Lenková
 */
public interface UserPreferencesListener {

	/**
	 * Called when the selected outputs change. Implementing classes should define
	 * the actions to take when this occurs.
	 */
	void onSelectedOutputsChanged();

	/**
	 * Called when the selected simulation GUI changes. Implementing classes should
	 * define the actions to take when this occurs.
	 */
	void onSelectedSimulationGUIChanged();
}
