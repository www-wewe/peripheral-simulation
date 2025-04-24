/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.ui;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for displaying user events.
 * 
 * This dialog is used to show the user events in the simulation.
 * 
 * @author Veronika Lenková
 */
public class UserEventDialog extends TitleAreaDialog {

	/** The events panel to display user events */
	private UserEventsPanel eventsPanel;

	/**
	 * Constructor for the UserEventDialog.
	 * 
	 * @param parentShell The parent shell for the dialog.
	 */
	public UserEventDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		eventsPanel = new UserEventsPanel(area, SWT.NONE);
		eventsPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return area;
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}
}
