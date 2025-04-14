package peripheralsimulation.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import peripheralsimulation.engine.UserEvent;
import peripheralsimulation.engine.UserEventType;
import peripheralsimulation.io.UserPreferences;
import peripheralsimulation.model.PeripheralModel;

/**
 * A panel (Composite) that lets the user define user events for the simulation.
 * They can specify start time, period, repeat count, register address, bit pos,
 * value, and event type. The "targetPeripheral" is the currently selected
 * peripheral from userPreferences.
 */
public class UserEventsPanel extends Composite {

	private UserPreferences userPreferences = UserPreferences.getInstance();
	private List<UserEvent> userEvents = new ArrayList<>();

	private Text txtStartTime;
	private Text txtPeriod;
	private Text txtRepeatCount;
	private Combo comboEventType;
	private Text txtRegisterAddress;
	private Text txtBitPosition;
	private Text txtValue;

	private Table tableEvents;

	public UserEventsPanel(Composite parent, int style) {
		super(parent, style);
		userEvents = userPreferences.getUserEvents();
		createContents();
	}

	private void createContents() {
		GridLayout layout = new GridLayout(2, false);
		this.setLayout(layout);

		// ========== Input fields ==========

		Label labelStartTime = new Label(this, SWT.NONE);
		labelStartTime.setText("Start Time:");
		txtStartTime = new Text(this, SWT.BORDER);
		txtStartTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label labelPeriod = new Label(this, SWT.NONE);
		labelPeriod.setText("Period (0 => one-time):");
		txtPeriod = new Text(this, SWT.BORDER);
		txtPeriod.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label labelRepeat = new Label(this, SWT.NONE);
		labelRepeat.setText("Repeat Count (0 => infinite ? test it):");
		txtRepeatCount = new Text(this, SWT.BORDER);
		txtRepeatCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label labelEventType = new Label(this, SWT.NONE);
		labelEventType.setText("Event Type:");
		comboEventType = new Combo(this, SWT.READ_ONLY);
		for (UserEventType et : UserEventType.values()) {
			comboEventType.add(et.name());
		}
		comboEventType.select(0);

		Label labelRegisterAddress = new Label(this, SWT.NONE);
		labelRegisterAddress.setText("Register Address (hex or dec):");
		txtRegisterAddress = new Text(this, SWT.BORDER);
		txtRegisterAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label labelBitPosition = new Label(this, SWT.NONE);
		labelBitPosition.setText("Bit Position to set/clear/toogle:");
		txtBitPosition = new Text(this, SWT.BORDER);
		txtBitPosition.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label labelValue = new Label(this, SWT.NONE);
		labelValue.setText("Value (for write to register):");
		txtValue = new Text(this, SWT.BORDER);
		txtValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// ========== "Add Event" button ==========

		new Label(this, SWT.NONE); // spacer
		Button buttonAdd = new Button(this, SWT.PUSH);
		buttonAdd.setText("Add Event");
		buttonAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addEvent();
			}
		});
		
		// ========== "Clear" button ==========

		new Label(this, SWT.NONE); // spacer
		Button buttonClear = new Button(this, SWT.PUSH);
		buttonClear.setText("Clear events");
		buttonClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				userEvents.clear();
				tableEvents.removeAll();
			}
		});

		// ========== Table of existing events ==========

		Label labelExisting = new Label(this, SWT.NONE);
		labelExisting.setText("Defined Events:");
		labelExisting.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		tableEvents = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		tableEvents.setHeaderVisible(true);
		tableEvents.setLinesVisible(true);
		GridData gdTable = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdTable.heightHint = 100;
		tableEvents.setLayoutData(gdTable);

		String[] colNames = { "StartTime", "Period", "Repeat", "Event type", "Register Address", "Bit position", "Value" };
		for (String col : colNames) {
			TableColumn tableCol = new TableColumn(tableEvents, SWT.NONE);
			tableCol.setText(col);
			tableCol.setWidth(90);
		}

		for (UserEvent event : userEvents) {
			TableItem item = new TableItem(tableEvents, SWT.NONE);
			item.setText(0, String.valueOf(event.getStartTime()));
			item.setText(1, String.valueOf(event.getPeriod()));
			item.setText(2, String.valueOf(event.getRepeatCount()));
			item.setText(3, event.getEventType().name());
			item.setText(4, String.format("0x%08X", event.getRegisterAddress()));
			item.setText(5, String.valueOf(event.getBitPosition()));
			item.setText(6, String.valueOf(event.getValue()));
		}
	}

	private void addEvent() {
		// Parse user input
		double startTime = parseDouble(txtStartTime.getText(), 0.0);
		double period = parseDouble(txtPeriod.getText(), 0.0);
		int repeatCount = parseInt(txtRepeatCount.getText(), 1);
		int registerAddress = parseHexOrDec(txtRegisterAddress.getText(), 0xE000E010);
		int bitPosition = parseInt(txtBitPosition.getText(), 0);
		int value = parseInt(txtValue.getText(), 0);

		String eventName = comboEventType.getItem(comboEventType.getSelectionIndex());
		UserEventType eventType = UserEventType.valueOf(eventName);

		// The targetPeripheral is already selected peripheral
		PeripheralModel target = userPreferences.getPeripheralModel();
		if (target == null) {
			MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR);
			messageBox.setMessage("No peripheral selected. Please select a peripheral first.");
			messageBox.open();
			return;
		}

		// Create new event
		UserEvent newEvt = new UserEvent(startTime, period, repeatCount, target, eventType, registerAddress, bitPosition, value);
		userEvents.add(newEvt);

		// Add to table
		TableItem item = new TableItem(tableEvents, SWT.NONE);
		item.setText(0, String.valueOf(startTime));
		item.setText(1, String.valueOf(period));
		item.setText(2, String.valueOf(repeatCount));
		item.setText(3, eventType.name());
		item.setText(4, String.format("0x%08X", registerAddress));
		item.setText(5, String.valueOf(bitPosition));
		item.setText(6, String.valueOf(value));

		// Set user events in user preferences
		userPreferences.setUserEvents(userEvents);
	}

	private double parseDouble(String stringToParse, double defaultValue) {
		try {
			return Double.parseDouble(stringToParse);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private int parseInt(String stringToParse, int defaultValue) {
		try {
			return Integer.parseInt(stringToParse);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private int parseHexOrDec(String stringToParse, int defaultValue) {
		if (stringToParse == null || stringToParse.trim().isEmpty()) {
			return defaultValue;
		}
		try {
			if (stringToParse.startsWith("0x") || stringToParse.startsWith("0X")) {
				return Integer.parseInt(stringToParse.substring(2), 16);
			}
			return Integer.parseInt(stringToParse);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Return the list of user-defined events. The parent dialog can then pass them
	 * to the engine or wherever needed.
	 */
	public List<UserEvent> getUserEvents() {
		return userEvents;
	}
}
