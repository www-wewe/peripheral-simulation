package peripheralsimulation.views;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import peripheralsimulation.io.UserPreferences;

public class SimulationTable implements SimulationGUI {

	/** The table in which the simulation results are displayed. Graph later. */
	private Table table;
	/** User preferences for the simulation. */
	private UserPreferences userPreferences = UserPreferences.getInstance();;
	/**
	 * Map of output values when output changes e.g. "INTERRUPT_LINE" -> [time and
	 * value, 0.5 false, 1.0 true, ...]
	 */
	private Map<String, Map<Double, Object>> outputsMap = new HashMap<>();
	/** Mapping of output names to column indices in the table. */
	private Map<String, Integer> outputToColumnIndex = new HashMap<>();

	public SimulationTable(Composite parent) {
		table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		table.setLayoutData(tableGridData);
	}

	@Override
	public void clear() {
		if (table != null && !table.isDisposed()) {
			table.removeAll();
		}
		outputsMap.clear();
	}

	@Override
	public void update(double timeValue, Map<String, Object> outputs) {
		// Build an array of strings for the row (one per column)
		int colCount = table.getColumnCount();
		assert colCount >= 1 : "Table must have at least one column";
		String[] rowText = new String[colCount];
		double scaledTime = timeValue * userPreferences.getTimeScaleFactor();
	    String prettyTime = userPreferences.getTimeFormat().format(scaledTime) + " " + userPreferences.getTimeUnits();

		rowText[0] = prettyTime;
		// true if at least one output changed (for the onlyChanges mode)
		boolean anyChange = false;

		// fill the user-chosen outputs
		for (String output : userPreferences.getSelectedOutputs()) {
			Integer colIndex = outputToColumnIndex.get(output);
			if (colIndex == null) {
				// output not mapped to a column => skip
				System.out.println("Output " + output + " not found in outputToColumnIndex");
				continue;
			}
			Object outputValue = outputs.get(output);
			rowText[colIndex] = (outputValue == null) ? "" : outputValue.toString();

			outputsMap.putIfAbsent(output, new LinkedHashMap<>());
			// mapa zmien outputu <time, output value>
			Map<Double, Object> outputMap = outputsMap.get(output);
			Object lastVal = outputMap.isEmpty() ? null : outputMap.values().toArray()[outputMap.size() - 1];

			if (lastVal == null || !lastVal.equals(outputValue)) {
				outputMap.put(timeValue, outputValue);
				anyChange = true;
			}

		}
		if (!userPreferences.isOnlyChanges() || anyChange) {
			createTableItem(outputs, rowText);
			System.out.println("Row: " + String.join(", ", rowText));
		}
		;
	}

	/**
	 * Create a new row in the table with the given text.
	 * 
	 * @param rowText
	 */
	private void createTableItem(Map<String, Object> outputs, String[] rowText) {
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(rowText);

		// If an interrupt is "true", highlight the row
		String output = "INTERRUPT_LINE";
		if (userPreferences.getSelectedOutputs().contains(output)) {
			String interruptVal = outputs.getOrDefault(output, "").toString();
			if (interruptVal.equals("true")) {
				item.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
			}
		}
		// iné outputy môžu byť tiež rôzne zvýraznené
	}

	/**
	 * Create columns in the table for each output in the given map.
	 * 
	 * @param outputs
	 */
	private void createColumnsInTable() {
		TableColumn timeColumn = new TableColumn(table, SWT.NONE);
		timeColumn.setText("Čas");
		timeColumn.setWidth(100);
		for (String output : userPreferences.getSelectedOutputs()) {
			createColumnForOutput(output);
		}
	}

	private void createColumnForOutput(String output) {
		TableColumn newCol = new TableColumn(table, SWT.NONE);
		newCol.setText(output);
		newCol.setWidth(100);
		// The index is the current total number of columns - 1
		int index = table.getColumnCount() - 1;
		outputToColumnIndex.put(output, index);
	}

	@Override
	public void setFocus() {
		table.setFocus();
	}

	public void onSelectedOutputsChanged() {
		Display.getDefault().syncExec(() -> {
			clear();
			outputToColumnIndex.clear();
			for (TableColumn column : table.getColumns()) {
				column.dispose();
			}
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
