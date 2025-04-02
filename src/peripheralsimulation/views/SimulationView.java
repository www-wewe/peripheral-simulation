package peripheralsimulation.views;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import jakarta.inject.Inject;
import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.io.UserPreferences;
import peripheralsimulation.io.UserPreferencesListener;
import peripheralsimulation.io.systick.SysTickRegisterDump;
import peripheralsimulation.io.systick.SysTickTimerConfig;
import peripheralsimulation.model.Peripheral;
import peripheralsimulation.model.PeripheralModel;
import peripheralsimulation.model.SCTimerModel;
import peripheralsimulation.model.SysTickTimerModel;
import peripheralsimulation.model.CounterModel;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

/**
 * View for simulation of peripherals.
 */
public class SimulationView extends ViewPart implements UserPreferencesListener {

	/** The ID of the view as specified by the extension. */
	public static final String ID = "peripheralsimulation.views.SimulationView";

	@Inject
	IWorkbench workbench;

	/** The table in which the simulation results are displayed. Graph later. */
	private Table table;
	/** Label for the status of the simulation. */
	private Label statusLabel;
	/** Buttons for running and stopping the simulation. */
	private Button runSimulationButton;
	private Button stopSimulationButton;
	/** Button for clearing the simulation. */
	private Button clearSimulationButton;
	/** The simulation engine. */
	private SimulationEngine simulationCore = new SimulationEngine(this::updateTable);
	/** The combo box for selecting the peripheral to simulate. */
	private Combo combo;
	/** Mapping of output names to column indices in the table. */
	private Map<String, Integer> outputToColumnIndex = new HashMap<>();
	/** User preferences for the simulation. */
	private UserPreferences userPreferences = new UserPreferences();
	// Map of output values when output changes
	// e.g. "INTERRUPT_LINE" -> [time and value, 0.5 false, 1.0 true, ...]
	private Map<String, Map<Double, Object>> outputsMap = new HashMap<>();
	/** Text for the status label when no simulation is running. */
	private static final String EMPTY_SIMULATION = "Kliknite na tlačidlo na spustenie simulácie...";

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(4, false);
		parent.setLayout(layout);
		createStatusLabel(parent);
		createButtons(parent);
		createTable(parent);
		userPreferences.addListener(this);
	}

	private void createStatusLabel(Composite parent) {
		statusLabel = new Label(parent, SWT.NONE);
		statusLabel.setText(EMPTY_SIMULATION);
		statusLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
	}

	private void createButtons(Composite parent) {
		// Start simulation button
		runSimulationButton = new Button(parent, SWT.PUSH);
		runSimulationButton.setText("Spustiť simuláciu");
		runSimulationButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		runSimulationButton.addListener(SWT.Selection, event -> runSimulation());
		if (simulationCore == null || !simulationCore.isSimulationRunning()) {
			runSimulationButton.setEnabled(true);
		} else {
			runSimulationButton.setEnabled(false);
		}

		// Stop simulation button
		stopSimulationButton = new Button(parent, SWT.PUSH);
		stopSimulationButton.setText("Zastaviť simuláciu");
		stopSimulationButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		stopSimulationButton.addListener(SWT.Selection, event -> stopSimulation());
		if (simulationCore != null && simulationCore.isSimulationRunning()) {
			stopSimulationButton.setEnabled(true);
		} else {
			stopSimulationButton.setEnabled(false);
		}

		// Clear simulation button
		clearSimulationButton = new Button(parent, SWT.PUSH);
		clearSimulationButton.setText("Vymazať simuláciu");
		clearSimulationButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		clearSimulationButton.addListener(SWT.Selection, event -> clearTable());

		// label ku comboboxu
		Label comboLabel = new Label(parent, SWT.NONE);
		comboLabel.setText("Vyberte perifériu: ");
		comboLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		// combobox na výber periférie
		combo = new Combo(parent, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		combo.add(Peripheral.SYSTICKTIMER.toString());
		combo.add(Peripheral.SCTIMER.toString());
		combo.add(Peripheral.COUNTER.toString());
		combo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSelectedPeripheralModel();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updateSelectedPeripheralModel();
			}
		});

		// button which opens the SelectOutputsDialog
		Button settingsButton = new Button(parent, SWT.PUSH);
		settingsButton.setText("Settings...");
		settingsButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		settingsButton.addListener(SWT.Selection, event -> {
			SettingsDialog dialog = new SettingsDialog(workbench.getActiveWorkbenchWindow().getShell(),
					userPreferences);
			dialog.open();
		});

	}

	private void clearTable() {
		table.removeAll();
		for (TableColumn column : table.getColumns()) {
			column.dispose();
		}
		outputToColumnIndex.clear();
		outputsMap.clear();
		statusLabel.setText(EMPTY_SIMULATION);
		runSimulationButton.setEnabled(true);
		stopSimulationButton.setEnabled(false);
	}

	private void createTable(Composite parent) {
		// Tabuľka na zobrazenie výstupu simulácie // TODO: SWTChart alebo JFreeChart
		table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		table.setLayoutData(tableGridData);
		// createColumnsInTable();
	}

	private PeripheralModel updateSelectedPeripheralModel() {
		PeripheralModel simulationModel;
		switch (combo.getText()) {
		case "System Tick Timer":
			SysTickRegisterDump dump = new SysTickRegisterDump();
			// fill in the fields from your exported data or from code
			SysTickTimerConfig config = new SysTickTimerConfig(dump);
			simulationModel = new SysTickTimerModel(config);
			break;
		case "SCTimer":
			simulationModel = new SCTimerModel();
			break;
		case "Counter":
			simulationModel = new CounterModel(255, // overflow value
					0, // initial value
					1000, // 1 kHz clock
					1 // prescaler
			);
			break;
		default:
			throw new IllegalArgumentException("Neznáma periféria.");
		}
		userPreferences.setPeripheralModel(simulationModel);
		return simulationModel;
	}

	private void updateTable(double timeValue, Map<String, Object> outputs) {
		Display.getDefault().asyncExec(() -> {
			if (!simulationCore.isSimulationRunning()) {
				return;
			}

			// Build an array of strings for the row (one per column)
			int colCount = table.getColumnCount();
			if (colCount == 0) {
				createColumnsInTable();
			}
			String[] rowText = new String[colCount];
			rowText[0] = String.valueOf(timeValue);
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
		});
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
		clearTable();
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

	/**
	 * Run simulation with the selected peripheral.
	 */
	private void runSimulation() {
		clearSimulationButton.setEnabled(false);
		createColumnsInTable();
		PeripheralModel simulationModel = userPreferences.getPeripheralModel();
		simulationCore.addPeripheral(simulationModel);

		Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia beží..."));
		Thread simulationThread = new Thread(() -> {
			try {
				simulationCore.initSimulation();
				simulationCore.startSimulation(Integer.MAX_VALUE); // FIXME: nekonečno?
				if (!simulationCore.isSimulationRunning()) {
					Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia dokončená."));
				}
//				runSimulationButton.setEnabled(false);
//				stopSimulationButton.setEnabled(false);
			} catch (Exception e) {
				e.printStackTrace();
				Display.getDefault().asyncExec(() -> statusLabel.setText("Chyba počas simulácie."));
			}
		});

		simulationThread.start();
		runSimulationButton.setEnabled(false);
		stopSimulationButton.setEnabled(true);
		clearSimulationButton.setEnabled(true);
	}

	private void stopSimulation() {
		simulationCore.stopSimulation();
		runSimulationButton.setEnabled(true);
		stopSimulationButton.setEnabled(false);
		clearSimulationButton.setEnabled(true);
		Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia zastavená."));
	}

	@Override
	public void setFocus() {
		table.setFocus();
	}

	@Override
	public void onPreferencesChanged() {
		Display.getDefault().asyncExec(this::createColumnsInTable);
	}

	@Override
	public void dispose() {
		userPreferences.removeListener(this);
		super.dispose();
	}
}
