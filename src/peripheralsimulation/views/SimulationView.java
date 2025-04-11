package peripheralsimulation.views;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.*;
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
import peripheralsimulation.ui.SettingsDialog;
import peripheralsimulation.ui.SimulationChart;
import peripheralsimulation.ui.SimulationGUI;
import peripheralsimulation.ui.SimulationTable;
import peripheralsimulation.model.CounterModel;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.part.ViewPart;

/**
 * View for simulation of peripherals.
 */
public class SimulationView extends ViewPart implements UserPreferencesListener {

	/** The ID of the view as specified by the extension. */
	public static final String ID = "peripheralsimulation.views.SimulationView";

	@Inject
	IWorkbench workbench;

	/** Label for the status of the simulation. */
	private Label statusLabel;
	/** Buttons for running and stopping the simulation. */
	private Button runSimulationButton;
	private Button stopSimulationButton;
	/** Button for clearing the simulation. */
	private Button clearSimulationButton;
	/** The simulation engine. */
	private SimulationEngine simulationEngine;
	/** The combo box for selecting the peripheral to simulate. */
	private Combo combo;
	/** User preferences for the simulation. */
	private UserPreferences userPreferences = UserPreferences.getInstance();
	/** The GUI for the simulation. */
	private SimulationGUI simulationGUI;
	/** Text for the status label when no simulation is running. */
	private static final String EMPTY_SIMULATION = "Kliknite na tlačidlo na spustenie simulácie...";

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(4, false);
		parent.setLayout(layout);
		createStatusLabel(parent);
		createButtons(parent);
		// createTable(parent);
		userPreferences.addListener(this);
		simulationGUI = updateSimulationGUI(parent);
		simulationEngine = new SimulationEngine(this::updateGUI);
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
		if (simulationEngine == null || !simulationEngine.isSimulationRunning()) {
			runSimulationButton.setEnabled(true);
		} else {
			runSimulationButton.setEnabled(false);
		}

		// Stop simulation button
		stopSimulationButton = new Button(parent, SWT.PUSH);
		stopSimulationButton.setText("Zastaviť simuláciu");
		stopSimulationButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		stopSimulationButton.addListener(SWT.Selection, event -> stopSimulation());
		if (simulationEngine != null && simulationEngine.isSimulationRunning()) {
			stopSimulationButton.setEnabled(true);
		} else {
			stopSimulationButton.setEnabled(false);
		}

		// Clear simulation button
		clearSimulationButton = new Button(parent, SWT.PUSH);
		clearSimulationButton.setText("Vymazať simuláciu");
		clearSimulationButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		clearSimulationButton.addListener(SWT.Selection, event -> clearGUI());

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
			SettingsDialog dialog = new SettingsDialog(workbench.getActiveWorkbenchWindow().getShell());
			dialog.open();
		});
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

	private SimulationGUI updateSimulationGUI(Composite parent) {
		// TODO: Tu to ešte robí problémy a niekedy nevytvorí nové GUI
		if (simulationGUI != null) {
			simulationGUI.dispose();
		}
		switch (userPreferences.getSelectedSimulationGUI()) {
		case TABLE:
			simulationGUI = new SimulationTable(parent);
			return simulationGUI;
		case GRAPH:
			simulationGUI = new SimulationChart(parent);
			return simulationGUI;
		default:
			return simulationGUI;
		}
	}

	/**
	 * Run simulation with the selected peripheral.
	 */
	private void runSimulation() {
		clearSimulationButton.setEnabled(false);
		PeripheralModel simulationModel = userPreferences.getPeripheralModel();
		simulationEngine.addPeripheral(simulationModel);

		Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia beží..."));
		Thread simulationThread = new Thread(() -> {
			try {
				simulationEngine.initSimulation();
				simulationEngine.startSimulation(userPreferences.getSimulationTimeRangeTo());
				if (!simulationEngine.isSimulationRunning()) {
					Display.getDefault().asyncExec(() -> {
						statusLabel.setText("Simulácia dokončená.");
						stopSimulationButton.setEnabled(false);
						clearSimulationButton.setEnabled(true);
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
				Display.getDefault().asyncExec(() -> statusLabel.setText("Chyba počas simulácie."));
			}
		});

		simulationThread.start();
		runSimulationButton.setEnabled(false);
		stopSimulationButton.setEnabled(true);
		clearSimulationButton.setEnabled(false);
	}

	private void stopSimulation() {
		simulationEngine.stopSimulation();
		runSimulationButton.setEnabled(false);
		stopSimulationButton.setEnabled(false);
		clearSimulationButton.setEnabled(true);
		Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia zastavená."));
	}

	private void clearGUI() {
		simulationGUI.clear();
		statusLabel.setText(EMPTY_SIMULATION);
		runSimulationButton.setEnabled(true);
		stopSimulationButton.setEnabled(false);
		clearSimulationButton.setEnabled(false);
	}

	private void updateGUI(double timeValue, Object[] outputs) {
		// TODO: predam iba selected outputs?
		Display.getDefault().asyncExec(() -> {
			double scaledTime = timeValue * userPreferences.getTimeScaleFactor();
			if (!simulationEngine.isSimulationRunning()) {
				return;
			}
			simulationGUI.update(scaledTime, outputs);
		});
	}

	@Override
	public void setFocus() {
		simulationGUI.setFocus();
	}

	@Override
	public void onSelectedOutputsChanged() {
		clearGUI();
		simulationGUI.onSelectedOutputsChanged();
	}

	@Override
	public void onSelectedSimulationGUIChanged() {
		clearGUI();
		simulationGUI = updateSimulationGUI(simulationGUI.getParent());
	}

	@Override
	public void dispose() {
		userPreferences.removeListener(this);
		super.dispose();
	}
}
