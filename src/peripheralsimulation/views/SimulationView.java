package peripheralsimulation.views;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.util.Map;

import org.eclipse.swt.SWT;
import jakarta.inject.Inject;
import peripheralsimulation.engine.SimulationEngine;
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
public class SimulationView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "peripheralsimulation.views.SimulationView";

	@Inject
	IWorkbench workbench;

	private Table table;
	private Label statusLabel;
	private Button runSimulationButton;
	private Button stopSimulationButton;
	private SimulationEngine simulationCore;
	private Combo combo;

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(3, false);
		parent.setLayout(layout);

		// Štítok na stav simulácie
		statusLabel = new Label(parent, SWT.NONE);
		statusLabel.setText("Kliknite na tlačidlo na spustenie simulácie...");
		statusLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		makeButtons(parent);

		// Tabuľka na zobrazenie výstupu simulácie // neskôr graf?
		table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		table.setLayoutData(tableGridData);

		// Stĺpce tabuľky
		TableColumn timeColumn = new TableColumn(table, SWT.NONE);
		timeColumn.setText("Čas");
		timeColumn.setWidth(100);

		TableColumn messageColumn = new TableColumn(table, SWT.NONE);
		messageColumn.setText("Správa");
		messageColumn.setWidth(300);
	}

	private void makeButtons(Composite parent) {

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
		combo.select(0);
	}

	private void runSimulation() {
		simulationCore = new SimulationEngine(this::updateTable);
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
		simulationCore.addPeripheral(simulationModel);

		Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia beží..."));
		Thread simulationThread = new Thread(() -> {
			try {
				simulationCore.initSimulation();
				simulationCore.startSimulation(Integer.MAX_VALUE); // FIXME: nekonečno?
				if (!simulationCore.isSimulationRunning()) {
					Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia dokončená."));
				}
			} catch (Exception e) {
				e.printStackTrace();
				Display.getDefault().asyncExec(() -> statusLabel.setText("Chyba počas simulácie."));
			}
			runSimulationButton.setEnabled(true);
			stopSimulationButton.setEnabled(false);
		});

		simulationThread.start();
		runSimulationButton.setEnabled(false);
		stopSimulationButton.setEnabled(true);
	}

	private void updateTable(double timeValue, Map<String, Object> outputs) {
		Display.getDefault().asyncExec(() -> {
			if (!simulationCore.isSimulationRunning()) {
				return;
			}
			// každý output má vlastný stlpec?
			TableItem item = new TableItem(table, SWT.NONE);
//			item.setText(new String[] { String.valueOf(timeValue), String.valueOf(counterValue) });
			String currentVal = outputs.getOrDefault("CURRENT", "").toString();
			String interruptVal = outputs.getOrDefault("INTERRUPT_LINE", "").toString();
			String rowText[] = { String.valueOf(timeValue), currentVal, interruptVal };
			item.setText(rowText);
			// If interrupt is true, set a background color:
			if (interruptVal.equals("true")) {
				item.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
			}
		});
	}

	private void stopSimulation() {
		simulationCore.stopSimulation();
		runSimulationButton.setEnabled(true);
		stopSimulationButton.setEnabled(false);
		Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia zastavená."));
	}

	@Override
	public void setFocus() {
		table.setFocus();
	}
}
