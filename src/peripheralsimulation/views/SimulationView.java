/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.views;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.*;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import jakarta.inject.Inject;
import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.engine.UserEvent;
import peripheralsimulation.io.UserPreferences;
import peripheralsimulation.io.UserPreferencesListener;
import peripheralsimulation.model.Peripheral;
import peripheralsimulation.model.PeripheralModel;
import peripheralsimulation.model.SysTickTimerModel;
import peripheralsimulation.model.flexio.FlexIOConfig;
import peripheralsimulation.model.systick.SysTickTimerConfig;
import peripheralsimulation.ui.SettingsDialog;
import peripheralsimulation.ui.SimulationChart;
import peripheralsimulation.ui.SimulationGUI;
import peripheralsimulation.ui.SimulationTable;
import peripheralsimulation.ui.UserEventDialog;
import peripheralsimulation.utils.RegisterMap;
import peripheralsimulation.utils.RegisterUtils;
import peripheralsimulation.model.CounterModel;
import peripheralsimulation.model.FlexIOModel;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.part.ViewPart;

/**
 * Class representing the simulation view in the Eclipse IDE. This class is
 * responsible for creating the user interface for the simulation, handling user
 * interactions, and managing the simulation engine.
 *
 * This class implements the UserPreferencesListener interface to respond to
 * changes in user preferences (e.g., selected outputs, simulation GUI).
 *
 * @author Veronika Lenková
 */
public class SimulationView extends ViewPart implements UserPreferencesListener {

	/** The ID of the view as specified by the extension. */
	public static final String ID = "peripheralsimulation.views.SimulationView";

	@Inject
	IWorkbench workbench;

	/** Label for the status of the simulation. */
	private Label statusLabel;
	/** Buttons for running the simulation. */
	private Button runSimulationButton;
	/** Button for stopping the simulation. */
	private Button stopSimulationButton;
	/** Button for clearing the simulation. */
	private Button clearSimulationButton;
	/** The simulation engine. */
	private SimulationEngine simulationEngine;
	/** The combo box for selecting the peripheral to simulate. */
	private Combo selectPeripheralCombo;
	/** User preferences for the simulation. */
	private UserPreferences userPreferences = UserPreferences.getInstance();
	/** The GUI for the simulation. */
	private SimulationGUI simulationGUI;
	/** Text for the run simulation button. */
	private static final String RUN_SIMULATION_BTN_TEXT = "Run simulation";
	/** Text for the stop simulation button. */
	private static final String STOP_SIMULATION_BTN_TEXT = "Stop simulation";
	/** Text for the clear simulation button. */
	private static final String CLEAR_SIMULATION_BTN_TEXT = "Clear simulation";
	/** Text for the status label when no simulation is running. */
	private static final String EMPTY_SIMULATION = "Click '" + RUN_SIMULATION_BTN_TEXT
			+ "' button to start simulation...";
	/** Text for the user events button. */
	private static final String USER_EVENTS_BTN_TEXT = "User Events...";
	/** Text for the settings button. */
	private static final String SETTINGS_BTN_TEXT = "Settings...";
	/** Text for selecting the peripheral in the combo box. */
	private static final String SELECT_PERIPHERAL_TEXT = "Select peripheral: ";
	/** Status label for simulation stopped. */
	private static final String STATUS_LABEL_SIMULATION_STOPPED = "Simulation stopped.";
	/** Status label for simulation failure. */
	private static final String STATUS_LABEL_SIMULATION_FAILURE = "Failure during simulation.";
	/** Status label for simulation finished. */
	private static final String STATUS_LABEL_SIMULATION_FINISHED = "Simulation finished.";
	/** Status label for running simulation. */
	private static final String STATUS_LABEL_RUNNING_SIMULATION = "Running simulation...";

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(4, false);
		parent.setLayout(layout);
		createStatusLabel(parent);
		createButtons(parent);
		userPreferences.addListener(this);
		simulationGUI = updateSimulationGUI(parent);
		simulationEngine = new SimulationEngine(this::updateGUI);
	}

	/**
	 * Create the status label for the simulation.
	 * 
	 * @param parent the parent composite
	 */
	private void createStatusLabel(Composite parent) {
		statusLabel = new Label(parent, SWT.NONE);
		statusLabel.setText(EMPTY_SIMULATION);
		statusLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
	}

	/**
	 * Create the buttons for running, stopping, clearing, selecting the peripheral,
	 * opening settings dialog and user events dialog.
	 * 
	 * @param parent the parent composite
	 */
	private void createButtons(Composite parent) {
		// Start simulation button
		runSimulationButton = new Button(parent, SWT.PUSH);
		runSimulationButton.setText(RUN_SIMULATION_BTN_TEXT);
		runSimulationButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		runSimulationButton.addListener(SWT.Selection, event -> runSimulation());
		if (simulationEngine == null || !simulationEngine.isSimulationRunning()) {
			runSimulationButton.setEnabled(true);
		} else {
			runSimulationButton.setEnabled(false);
		}

		// Stop simulation button
		stopSimulationButton = new Button(parent, SWT.PUSH);
		stopSimulationButton.setText(STOP_SIMULATION_BTN_TEXT);
		stopSimulationButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		stopSimulationButton.addListener(SWT.Selection, event -> stopSimulation());
		if (simulationEngine != null && simulationEngine.isSimulationRunning()) {
			stopSimulationButton.setEnabled(true);
		} else {
			stopSimulationButton.setEnabled(false);
		}

		// Clear simulation button
		clearSimulationButton = new Button(parent, SWT.PUSH);
		clearSimulationButton.setText(CLEAR_SIMULATION_BTN_TEXT);
		clearSimulationButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		clearSimulationButton.addListener(SWT.Selection, event -> clearGUI());

		// Combobox for selecting the peripheral
		Label comboLabel = new Label(parent, SWT.NONE);
		comboLabel.setText(SELECT_PERIPHERAL_TEXT);
		comboLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		selectPeripheralCombo = new Combo(parent, SWT.READ_ONLY);
		selectPeripheralCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		for (Peripheral peripheral : Peripheral.values()) {
			selectPeripheralCombo.add(peripheral.toString());
		}
		selectPeripheralCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSelectedPeripheralModel();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updateSelectedPeripheralModel();
			}
		});

		// Button which opens the SettingsDialog
		Button settingsButton = new Button(parent, SWT.PUSH);
		settingsButton.setText(SETTINGS_BTN_TEXT);
		settingsButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		settingsButton.addListener(SWT.Selection, event -> {
			SettingsDialog dialog = new SettingsDialog(workbench.getActiveWorkbenchWindow().getShell());
			dialog.open();
		});

		// Button which opens the UserEventDialog
		Button userEventButton = new Button(parent, SWT.PUSH);
		userEventButton.setText(USER_EVENTS_BTN_TEXT);
		userEventButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		userEventButton.addListener(SWT.Selection, event -> {
			UserEventDialog dialog = new UserEventDialog(workbench.getActiveWorkbenchWindow().getShell());
			dialog.open();
		});
	}

	/**
	 * Update the selected peripheral model based on the selected peripheral in the
	 * combo box.
	 * 
	 * @return the selected peripheral model
	 */
	private PeripheralModel updateSelectedPeripheralModel() {
		PeripheralModel simulationModel;
		Peripheral selectedPeripheral = Peripheral.fromDisplayName(selectPeripheralCombo.getText());
		switch (selectedPeripheral) {
		case SYSTICKTIMER:
			// fill in the fields from your exported data or from code
			SysTickTimerConfig config = new SysTickTimerConfig(0x3, // SYST_CSR
					0x0001D4BF, // SYST_RVR
					0, // SYST_CVR
					0, // SYST_CALIB
					48e6, // mainClk
					12e6 // externalClk
			);
			simulationModel = new SysTickTimerModel(config);
			break;
		case COUNTER:
			simulationModel = new CounterModel(255, // overflow value
					0, // initial value
					1000, // 1 kHz clock
					1 // prescaler
			);
			break;
		case FLEXIO:
			Map<String, Integer> flexioRegisters = RegisterUtils.loadRegistersFromCsv();
			RegisterMap registerMap = RegisterUtils.convertToFlexIORegisterMap(flexioRegisters);
			FlexIOConfig flexioConfig = new FlexIOConfig(registerMap);
			simulationModel = new FlexIOModel(flexioConfig);
			break;
		default:
			throw new IllegalArgumentException("Unknown peripheral.");
		}
		userPreferences.setPeripheralModel(simulationModel);
		return simulationModel;
	}

	/**
	 * Update the simulation GUI based on the selected simulation GUI in user
	 * preferences.
	 * 
	 * @param parent the parent composite
	 * @return the selected simulation GUI
	 */
	private SimulationGUI updateSimulationGUI(Composite parent) {
		if (simulationGUI != null) {
			simulationGUI.dispose();
		}
		switch (userPreferences.getSelectedSimulationGUI()) {
		case TABLE:
			simulationGUI = new SimulationTable(parent);
			parent.layout(true, true);
			return simulationGUI;
		case GRAPH:
			simulationGUI = new SimulationChart(parent);
			parent.layout(true, true);
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
		simulationEngine.cleanSimulation();
		PeripheralModel simulationModel = userPreferences.getPeripheralModel();
		simulationEngine.setPeripheralModel(simulationModel);
//		UserEvent userEvent = new UserEvent(0.010, // start time
//				0.010, // period, means every 10ms
//				5, // repeat count (0 means infinite)
//				simulationModel, // target peripheral
//				UserEventType.TOGGLE_BIT, // event type
//				0xE000E010, // register address - SYST_CSR
//				1, // bit position - toggling bit #3 - enable
//				0 // write value, not used for toggle
//		);
//		simulationEngine.addUserEvent(userEvent);

		for (UserEvent userEvent : userPreferences.getUserEvents()) {
			simulationEngine.addUserEvent(userEvent);
		}

		Display.getDefault().asyncExec(() -> statusLabel.setText(STATUS_LABEL_RUNNING_SIMULATION));
		Thread simulationThread = new Thread(() -> {
			try {
				simulationEngine.initSimulation();
				simulationEngine.startSimulation(userPreferences.getSimulationTimeRangeTo());
				if (!simulationEngine.isSimulationRunning()) {
					Display.getDefault().asyncExec(() -> {
						if (simulationGUI instanceof SimulationChart) {
							((SimulationChart) simulationGUI).redrawAllSeries();
						}
						statusLabel.setText(STATUS_LABEL_SIMULATION_FINISHED);
						stopSimulationButton.setEnabled(false);
						clearSimulationButton.setEnabled(true);
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
				Display.getDefault().asyncExec(() -> statusLabel.setText(STATUS_LABEL_SIMULATION_FAILURE));
			}
		});

		simulationThread.start();
		runSimulationButton.setEnabled(false);
		stopSimulationButton.setEnabled(true);
		clearSimulationButton.setEnabled(false);
	}

	/**
	 * Stop the simulation.
	 */
	private void stopSimulation() {
		simulationEngine.stopSimulation();
		runSimulationButton.setEnabled(false);
		stopSimulationButton.setEnabled(false);
		clearSimulationButton.setEnabled(true);
		Display.getDefault().asyncExec(() -> statusLabel.setText(STATUS_LABEL_SIMULATION_STOPPED));
	}

	/**
	 * Clear the simulation GUI.
	 */
	private void clearGUI() {
		simulationGUI.clear();
		statusLabel.setText(EMPTY_SIMULATION);
		runSimulationButton.setEnabled(true);
		stopSimulationButton.setEnabled(false);
		clearSimulationButton.setEnabled(false);
	}

	/**
	 * Update the GUI with the simulation time and outputs.
	 * 
	 * @param timeValue the simulation time
	 * @param outputs   the simulation outputs
	 */
	private void updateGUI(double timeValue, Object[] outputs) {
		Display.getDefault().asyncExec(() -> {
			double scaledTime = timeValue * userPreferences.getTimeScaleFactor();
//			if (!simulationEngine.isSimulationRunning()) {
//				return;
//			}
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
