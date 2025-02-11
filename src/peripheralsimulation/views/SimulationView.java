package peripheralsimulation.views;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.SWT;
import jakarta.inject.Inject;
import peripheralsimulation.CounterSimulation;
import peripheralsimulation.PeripheralSimulation;
import peripheralsimulation.SCTimerSimulation;
import peripheralsimulation.engine.SimulationCore;
import peripheralsimulation.model.Peripheral;

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
	private Button pauseSimulationButton;
	private Button stopSimulationButton;
	private SimulationCore simulationCore;
	private Combo combo;

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		parent.setLayout(layout);

		// Štítok na stav simulácie
		statusLabel = new Label(parent, SWT.NONE);
		statusLabel.setText("Kliknite na tlačidlo na spustenie simulácie...");
		statusLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		makeButtons(parent);

		// Tabuľka na zobrazenie výstupu simulácie
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
		// Pause simulation button
		pauseSimulationButton = new Button(parent, SWT.PUSH);
		pauseSimulationButton.setText("Pauznúť simuláciu");
		pauseSimulationButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		pauseSimulationButton.addListener(SWT.Selection, event -> pauseSimulation());
		if (simulationCore != null && simulationCore.isSimulationRunning()) {
			pauseSimulationButton.setEnabled(true);
		} else {
			pauseSimulationButton.setEnabled(false);
		}

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
		comboLabel.setText("Vyberte perifériu:");
		comboLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		// combobox na výber periférie
		combo = new Combo(parent, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		combo.add(Peripheral.SCTIMER.toString());
		combo.add(Peripheral.COUNTER.toString());
		combo.select(0);
	}

	private void runSimulation() {
		if (simulationCore == null) {
			PeripheralSimulation peripheralSimulation;
			switch (combo.getText()) {
			case "SCTimer":
				peripheralSimulation = new SCTimerSimulation();
				break;
			case "Counter":
				peripheralSimulation = new CounterSimulation();
				break;
			default:
				throw new IllegalArgumentException("Neznáma periféria.");
			}
			simulationCore = new SimulationCore(peripheralSimulation, this::updateTable);
		}
		Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia beží..."));
		Thread simulationThread = new Thread(() -> {
			try {
				simulationCore.startSimulation(Integer.MAX_VALUE); // FIXME: nekonečno?
				if (!simulationCore.isSimulationPaused()) {
					Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia dokončená."));
				}
			} catch (Exception e) {
				e.printStackTrace();
				Display.getDefault().asyncExec(() -> statusLabel.setText("Chyba počas simulácie."));
			}
		});

		simulationThread.start();

		runSimulationButton.setEnabled(false);
		pauseSimulationButton.setEnabled(true);
		stopSimulationButton.setEnabled(true);
	}

	private void updateTable(Double currentTime, String outputMessage) {
		Display.getDefault().asyncExec(() -> {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(new String[] { String.valueOf(currentTime), outputMessage });
		});
	}

	private void pauseSimulation() {
		simulationCore.pauseSimulation();
		runSimulationButton.setEnabled(true);
		pauseSimulationButton.setEnabled(false);
		Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia pozastavená."));
	}

	private void stopSimulation() {
		simulationCore.stopSimulation();
		runSimulationButton.setEnabled(true);
		pauseSimulationButton.setEnabled(false);
		stopSimulationButton.setEnabled(false);
		Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia zastavená."));
	}

	@Override
	public void setFocus() {
		table.setFocus();
	}
}
