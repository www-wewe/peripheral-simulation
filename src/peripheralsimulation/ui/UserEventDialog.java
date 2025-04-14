package peripheralsimulation.ui;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class UserEventDialog extends TitleAreaDialog {

    private UserEventsPanel eventsPanel;

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

