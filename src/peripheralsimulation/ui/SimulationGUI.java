/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.ui;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface for the simulation GUI. Defines methods for updating the GUI,
 * clearing it, setting focus, handling output selection changes, and disposing
 * of the GUI.
 *
 * @author Veronika Lenková
 */
public interface SimulationGUI {

	/**
	 * Updates the GUI with the given time value and output values.
	 * 
	 * @param timeValue The current time value of the simulation.
	 * @param outputs   The output values to be displayed in the GUI.
	 */
	public void update(double timeValue, Object[] outputs);

	/**
	 * Clears the GUI, removing all displayed data.
	 */
	public void clear();

	/**
	 * Sets the focus to the GUI component.
	 */
	public void setFocus();

	/**
	 * Handles changes in the selected outputs. Implementing classes should define
	 * the actions to take when this occurs.
	 */
	public void onSelectedOutputsChanged();

	/**
	 * Returns the parent composite of the GUI component.
	 * 
	 * @return The parent composite of the GUI component.
	 */
	public Composite getParent();

	/**
	 * Disposes of the GUI component, releasing any resources it holds.
	 */
	public void dispose();

}
