package peripheralsimulation.views;

import java.util.Map;

import org.eclipse.swt.widgets.Composite;

public interface SimulationGUI {
	
	//public void create(Composite parent);
	
	public void update(double timeValue, Map<String, Object> outputs);
	
	public void clear();

	public void setFocus();

	public void onSelectedOutputsChanged();

	public Composite getParent();
	
	public void dispose();

}
