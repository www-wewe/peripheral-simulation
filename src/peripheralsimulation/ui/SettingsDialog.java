/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import peripheralsimulation.io.UserPreferences;
import peripheralsimulation.model.CounterModel;
import peripheralsimulation.model.FlexIOModel;
import peripheralsimulation.model.Peripheral;
import peripheralsimulation.model.PeripheralModel;
import peripheralsimulation.model.SysTickTimerModel;
import peripheralsimulation.model.flexio.FlexIOConfig;
import peripheralsimulation.model.systick.SysTickTimerConfig;
import peripheralsimulation.utils.RegisterMap;
import peripheralsimulation.utils.RegisterUtils;

/**
 * Dialog for setting user preferences for the simulation.
 * 
 * @author Veronika Lenková
 */
public class SettingsDialog extends Dialog {
	/** The user preferences instance */
	private UserPreferences userPreferences;
	/** List of checkboxes for selecting outputs */
	private List<Button> checkboxes = new ArrayList<>();
	/** Checkbox for showing only changes */
	private Button onlyChanges;
	/** Text field for milliseconds to wait between iterations */
	private Text millisToWaitTextField;
	/** Combo box for selecting the simulation visualization */
	private Combo visualizationSelectionCombo;
	/** Text field for monitoring period */
	private Text monitoringPeriodTextField;
	/** Text field for simulation time range from */
	private Text simulationTimeRangeFromTextField;
	/** Text field for simulation time range to */
	private Text simulationTimeRangeToTextField;
	/** Text field for clock frequency */
	private Text clockFrequencyTextField;
	/** Combo box for selecting the time scale */
	private Combo timeScaleCombo;

	/**
	 * Constructor for the SettingsDialog.
	 * 
	 * @param parentShell The parent shell for the dialog.
	 */
	public SettingsDialog(Shell parentShell) {
		super(parentShell);
		this.userPreferences = UserPreferences.getInstance();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialog = (Composite) super.createDialogArea(parent);
		dialog.setLayout(new GridLayout(1, false));
		addCheckboxes(dialog);
		addTextFieldMillisToWait(dialog);
		addTextFieldMonitoringFreq(dialog);
		addTextFieldsForTimeRange(dialog);
		addTextFieldForClockFrequency(dialog);
		addTimeScaleSelection(dialog);
		addGuiSelection(dialog);
		addImportButton(dialog);
		return dialog;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Settings...");
	}

	@Override
	protected void okPressed() {
		SimulationGuiChoice selectedGui = SimulationGuiChoice.fromDisplayName(visualizationSelectionCombo.getText());
		userPreferences.setSelectedSimulationGUI(selectedGui);

		List<String> selectedOutputs = new ArrayList<>();
		for (Button checkbox : checkboxes) {
			if (checkbox.getSelection()) {
				String output = checkbox.getText();
				selectedOutputs.add(output);
			}
		}
		userPreferences.setSelectedOutputs(selectedOutputs);
		userPreferences.setOnlyChanges(onlyChanges.getSelection());

		String millisToWait = millisToWaitTextField.getText();
		if (millisToWait.isEmpty()) {
			millisToWait = "0";
		}
		try {
			Long.parseLong(millisToWait);
		} catch (NumberFormatException e) {
			millisToWait = "0";
		}
		userPreferences.setMillisToWait(Long.parseLong(millisToWait));
		// TODO: verify inputs
		userPreferences.setMonitoringPeriod(Double.parseDouble(monitoringPeriodTextField.getText()));
		userPreferences.setSimulationTimeRangeFrom(Double.parseDouble(simulationTimeRangeFromTextField.getText()));
		userPreferences.setSimulationTimeRangeTo(Double.parseDouble(simulationTimeRangeToTextField.getText()));
		userPreferences.setClockFrequency(Integer.parseInt(clockFrequencyTextField.getText()) * 1_000_000);
		userPreferences.setTimeScaleUnits(timeScaleCombo.getText());
		super.okPressed();
	}

	/**
	 * Adds checkboxes for each output of the selected peripheral.
	 * 
	 * @param dialog The dialog to which the checkboxes will be added.
	 */
	private void addCheckboxes(Composite dialog) {
		PeripheralModel selectedPeripheral = userPreferences.getPeripheralModel();
		if (selectedPeripheral != null) {
			String[] outputs = selectedPeripheral.getOutputNames();
			for (String output : outputs) {
				Button checkbox = new Button(dialog, SWT.CHECK);
				checkbox.setText(output);
				checkbox.setSelection(userPreferences.getSelectedOutputs().contains(output));
				checkboxes.add(checkbox);
			}
		}
		onlyChanges = new Button(dialog, SWT.CHECK);
		onlyChanges.setText("Show only changes");
		onlyChanges.setSelection(userPreferences.isOnlyChanges());
	}

	/**
	 * Add text field for milliseconds to wait between each simulation step.
	 * 
	 * @param dialog The dialog to which the text field will be added.
	 */
	private void addTextFieldMillisToWait(Composite dialog) {
		Label label = new Label(dialog, SWT.NONE);
		label.setText("Milliseconds to wait between each simulation step:");

		millisToWaitTextField = new Text(dialog, SWT.BORDER);
		millisToWaitTextField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		millisToWaitTextField.setText(String.valueOf(userPreferences.getMillisToWait()));
	}

	/**
	 * Add combo box for selecting the time scale.
	 *
	 * @param dialog The dialog to which the combo box will be added.
	 */
	private void addTimeScaleSelection(Composite dialog) {
		Label label = new Label(dialog, SWT.NONE);
		label.setText("Time units to display:");

		timeScaleCombo = new Combo(dialog, SWT.READ_ONLY);
		timeScaleCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		timeScaleCombo.add(UserPreferences.TIME_MS);
		timeScaleCombo.add(UserPreferences.TIME_US);
		timeScaleCombo.add(UserPreferences.TIME_NS);
		timeScaleCombo.setToolTipText("Time scale for the simulation.");
		String currentUnits = userPreferences.getTimeUnits();
		int index = timeScaleCombo.indexOf(currentUnits);
		if (index != -1) {
			timeScaleCombo.select(index);
		} else {
			timeScaleCombo.select(0); // Default to the first one if not found
		}
	}

	/**
	 * Add combo box for selecting the simulation visualization.
	 * 
	 * @param dialog The dialog to which the combo box will be added.
	 */
	private void addGuiSelection(Composite dialog) {
		// label ku comboboxu
		Label comboLabel = new Label(dialog, SWT.NONE);
		comboLabel.setText("Select GUI: ");
		comboLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		// combobox na výber vizualizácie
		visualizationSelectionCombo = new Combo(dialog, SWT.READ_ONLY);
		visualizationSelectionCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		visualizationSelectionCombo.add(SimulationGuiChoice.TABLE.toString());
		visualizationSelectionCombo.add(SimulationGuiChoice.GRAPH.toString());
		visualizationSelectionCombo.select(userPreferences.getSelectedSimulationGUI().ordinal());
	}

	/**
	 * Add text field for monitoring frequency. (How often the simulation should be
	 * updated)
	 * 
	 * @param dialog The dialog to which the text field will be added.
	 */
	private void addTextFieldMonitoringFreq(Composite dialog) {
		Label label = new Label(dialog, SWT.NONE);
		label.setText("Monitoring period in seconds:");

		monitoringPeriodTextField = new Text(dialog, SWT.BORDER);
		monitoringPeriodTextField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		monitoringPeriodTextField.setText(String.valueOf(userPreferences.getMonitoringPeriod()));
		monitoringPeriodTextField
				.setToolTipText("Monitoring period in seconds. The simulation will be updated every X seconds.");
	}

	/**
	 * Add text fields (from, to) for time range.
	 * 
	 * @param dialog The dialog to which the text fields will be added.
	 */
	private void addTextFieldsForTimeRange(Composite dialog) {
		Label label = new Label(dialog, SWT.NONE);
		label.setText("Simulation time range (from, to in seconds):");

		Composite timeRangeComposite = new Composite(dialog, SWT.NONE);
		timeRangeComposite.setLayout(new GridLayout(2, false));

		simulationTimeRangeFromTextField = new Text(timeRangeComposite, SWT.BORDER);
		simulationTimeRangeFromTextField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		simulationTimeRangeFromTextField.setText(String.valueOf(userPreferences.getSimulationTimeRangeFrom()));

		simulationTimeRangeToTextField = new Text(timeRangeComposite, SWT.BORDER);
		simulationTimeRangeToTextField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		simulationTimeRangeToTextField.setText(String.valueOf(userPreferences.getSimulationTimeRangeTo()));
	}

	/**
	 * Add text field for clock frequency.
	 * 
	 * @param dialog The dialog to which the text field will be added.
	 */
	private void addTextFieldForClockFrequency(Composite dialog) {
		Label label = new Label(dialog, SWT.NONE);
		label.setText("Clock frequency in MHz:");

		clockFrequencyTextField = new Text(dialog, SWT.BORDER);
		clockFrequencyTextField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		clockFrequencyTextField.setText(String.valueOf(userPreferences.getClockFrequency() / 1_000_000));
		clockFrequencyTextField
				.setToolTipText("Clock frequency in MHz. The simulation will be updated every X seconds.");
	}

	/**
	 * Add button for importing registers from CSV file.
	 *
	 * @param dialog The dialog to which the button will be added.
	 */
	private void addImportButton(Composite dialog) {
		Button importBtn = new Button(dialog, SWT.PUSH);
		importBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		importBtn.setText("Import of registers");
		importBtn.addListener(SWT.Selection, e -> {
			Map<String, Integer> newRegs = RegisterUtils.loadRegistersFromCsv();
			if (!newRegs.isEmpty()) {
				RegisterMap registerMap;
				PeripheralModel peripheralModel = userPreferences.getPeripheralModel();
				Peripheral peripheral = userPreferences.getSelectedPeripheralType();
				switch (peripheral) {
				case SYSTICKTIMER:
					registerMap = RegisterUtils.convertToSysTickRegisterMap(newRegs);
					SysTickTimerConfig config = new SysTickTimerConfig(registerMap, 48e6, // mainClk
							12e6 // externalClk
					);
					peripheralModel = new SysTickTimerModel(config);
					break;
				case COUNTER:
					peripheralModel = new CounterModel(255, // overflow value
							0, // initial value
							1000, // 1 kHz clock
							1 // prescaler
					);
					break;
				case FLEXIO:
					registerMap = RegisterUtils.convertToFlexIORegisterMap(newRegs);
					FlexIOConfig flexioConfig = new FlexIOConfig(registerMap);
					peripheralModel = new FlexIOModel(flexioConfig);
					break;
				default:
					throw new IllegalArgumentException("Unknown peripheral.");
				}
				userPreferences.setPeripheralModel(peripheralModel);
			}
		});
	}

}
