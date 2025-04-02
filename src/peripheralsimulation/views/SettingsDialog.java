package peripheralsimulation.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import peripheralsimulation.io.UserPreferences;
import peripheralsimulation.model.PeripheralModel;

public class SettingsDialog extends Dialog {

	private UserPreferences userPreferences;
	private List<Button> checkboxes = new ArrayList<>();
	private Button onlyChanges;
	private Text millisToWaitText;


	public SettingsDialog(Shell parentShell, UserPreferences userPreferences) {
		super(parentShell);
		this.userPreferences = userPreferences;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialog = (Composite) super.createDialogArea(parent);
		dialog.setLayout(new GridLayout(1, false));
		addCheckboxes(dialog);
		addTextFieldMillisToWait(dialog);
		return dialog;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Settings...");
	}

	@Override
	protected void okPressed() {
		Set<String> selectedOutputs = new HashSet<>();
		for (Button checkbox : checkboxes) {
			if (checkbox.getSelection()) {
				selectedOutputs.add(checkbox.getText());
			}
		}
		userPreferences.setSelectedOutputs(selectedOutputs);
		userPreferences.setOnlyChanges(onlyChanges.getSelection());
		UserPreferences.setMillisToWait(Long.parseLong(millisToWaitText.getText()));
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
			// Assuming userPreferences has a method to get outputs for a peripheral
			Set<String> outputs = selectedPeripheral.getOutputs();
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

	private void addTextFieldMillisToWait(Composite dialog) {
		Label label = new Label(dialog, SWT.NONE);
	    label.setText("Milliseconds to wait:");

	    millisToWaitText = new Text(dialog, SWT.BORDER);
	    millisToWaitText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	    millisToWaitText.setText(String.valueOf(UserPreferences.getMillisToWait()));
	}
}
