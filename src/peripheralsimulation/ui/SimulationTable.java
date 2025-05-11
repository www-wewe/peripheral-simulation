/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import peripheralsimulation.io.UserPreferences;
import peripheralsimulation.model.PeripheralModel;

/**
 * Class for displaying a simulation table using SWT.
 * 
 * This class implements the SimulationGUI interface and provides methods to
 * update and clear the table.
 *
 * @author Veronika Lenková
 */
public class SimulationTable implements SimulationGUI {

	/** The table in which the simulation results are displayed. Graph later. */
	private Table table;
	/** User preferences for the simulation. */
	private UserPreferences userPreferences = UserPreferences.getInstance();
	/** The simulation model. */
	private PeripheralModel peripheralModel;
	/**
	 * Map of output values when output changes e.g. "INTERRUPT" -> [time and value,
	 * 0.5 false, 1.0 true, ...]
	 */
	// private Map<String, Map<Double, Object>> outputsMap = new HashMap<>();
	/** The last output values for each selected output. */
	private Object[] lastOutputValues = new Object[userPreferences.getSelectedOutputs().size()];

	/**
	 * Constructor for the SimulationTable.
	 * 
	 * @param parent The parent composite for the table.
	 */
	public SimulationTable(Composite parent) {
		table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		table.setLayoutData(tableGridData);
		lastOutputValues = new Object[userPreferences.getSelectedOutputs().size()];
	}

	@Override
	public void clear() {
		if (table != null && !table.isDisposed()) {
			table.removeAll();
		}
		// outputsMap.clear();
		lastOutputValues = new Object[userPreferences.getSelectedOutputs().size()];
	}

	@Override
	public void update(double timeValue, Object[] outputs) {
		// Build an array of strings for the row (one per column)
		int colCount = table.getColumnCount();
		if (colCount == 0) {
			createColumnsInTable();
			colCount = table.getColumnCount();
		}
		String[] rowText = new String[colCount];
		rowText[0] = formatTime(timeValue);
		// true if at least one output changed (for the onlyChanges mode)
		boolean anyChange = false;

		// fill the user-chosen outputs
		int[] selectedIndices = userPreferences.getSelectedOutputsIndices();
		for (int i = 0; i < selectedIndices.length; i++) {
			int outputIndex = selectedIndices[i];
			Object outputValue = outputs[outputIndex];
			rowText[i + 1] = (outputValue == null) ? "" : outputValue.toString();

			// output =
			// userPreferences.getPeripheralModel().getOutputName(selectedOutputsIndices.get(i));
			// outputsMap.putIfAbsent(output, new LinkedHashMap<>());
			// Map<Double, Object> outputMap = outputsMap.get(output);
			// Object lastValue = outputMap.isEmpty() ? null :
			// outputMap.values().toArray()[outputMap.size() - 1];
			Object lastValue = lastOutputValues[i];

			if (lastValue == null || !lastValue.equals(outputValue)) {
				// outputMap.put(timeValue, outputValue);
				lastOutputValues[i] = outputValue;
				anyChange = true;
			}
		}

		if (!userPreferences.isOnlyChanges() || anyChange) {
			createTableItem(outputs, rowText);
			System.out.println("Row: " + String.join(", ", rowText));
		}
	}

	/**
	 * Format the time value to a string using the user's preferred format.
	 * 
	 * @param timeValue the time value to format
	 * @return the formatted time string
	 */
	private String formatTime(double timeValue) {
		return userPreferences.getTimeFormat().format(timeValue) + " " + userPreferences.getTimeUnits();
	}

	/**
	 * Create a new row in the table with the given text.
	 *
	 * @param outputs The output values for the row.
	 * @param rowText The text to display in the row.
	 */
	private void createTableItem(Object[] outputs, String[] rowText) {
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(rowText);

		// If an interrupt is "true", highlight the row
		String output = "INTERRUPT";
		if (userPreferences.getSelectedOutputs().contains(output)) {
			String interruptVal = outputs[peripheralModel.getOutputIndex(output)].toString();
			if (interruptVal.equals("true")) {
				item.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
			}
		}
		// other outputs can be highlighted as well
	}

	/**
	 * Create columns in the table based on the user's selected outputs.
	 */
	private void createColumnsInTable() {
		TableColumn timeColumn = new TableColumn(table, SWT.NONE);
		timeColumn.setText("Čas");
		timeColumn.setWidth(100);
		for (String output : userPreferences.getSelectedOutputs()) {
			createColumnForOutput(output);
		}
	}

	/**
	 * Create a new column in the table for the given output.
	 *
	 * @param output The name of the output.
	 */
	private void createColumnForOutput(String output) {
		TableColumn newCol = new TableColumn(table, SWT.NONE);
		newCol.setText(output);
		newCol.setWidth(100);
	}

	@Override
	public void setFocus() {
		table.setFocus();
	}

	@Override
	public void onSelectedOutputsChanged() {
		if (table == null || table.isDisposed()) {
			return;
		}
		Display.getDefault().syncExec(() -> {
			for (TableColumn column : table.getColumns()) {
				column.dispose();
			}
			lastOutputValues = new Object[userPreferences.getSelectedOutputs().size()];
			peripheralModel = userPreferences.getPeripheralModel();
			createColumnsInTable();
		});
	}

	@Override
	public Composite getParent() {
		return table.getParent();
	}

	@Override
	public void dispose() {
		clear();
		table.dispose();
	}
}
