package peripheralsimulation.ui;

import org.eclipse.swt.widgets.Composite;

public interface SimulationGUI {

	public void update(double timeValue, Object[] outputs);

	public void clear();

	public void setFocus();

	public void onSelectedOutputsChanged();

	public Composite getParent();

	public void dispose();

}
