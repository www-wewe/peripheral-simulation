package peripheralsimulation.views;


import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.SWT;
import jakarta.inject.Inject;
import peripheralsimulation.PeripheralSimulation;
import peripheralsimulation.engine.SimulationCore;

import org.eclipse.swt.layout.FillLayout;
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
    private SimulationCore simulationCore;

	 @Override
	    public void createPartControl(Composite parent) {
	        // Nastavenie layoutu
	        parent.setLayout(new FillLayout(SWT.VERTICAL));

	        // Štítok na stav simulácie
	        statusLabel = new Label(parent, SWT.NONE);
	        statusLabel.setText("Kliknite na tlačidlo na spustenie simulácie...");

	        // Tlačidlo na spustenie simulácie
	        runSimulationButton = new Button(parent, SWT.PUSH);
	        runSimulationButton.setText("Spustiť simuláciu");

	        // Tabuľka na zobrazenie výstupu simulácie
	        table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	        table.setHeaderVisible(true);
	        table.setLinesVisible(true);

	        // Stĺpce tabuľky
	        TableColumn timeColumn = new TableColumn(table, SWT.NONE);
	        timeColumn.setText("Čas");
	        timeColumn.setWidth(100);

	        TableColumn messageColumn = new TableColumn(table, SWT.NONE);
	        messageColumn.setText("Správa");
	        messageColumn.setWidth(300);

	        // Nastavenie akcie na stlačenie tlačidla
	        runSimulationButton.addListener(SWT.Selection, event -> runSimulation());
	    }

	 private void runSimulation() {
		 PeripheralSimulation model = new PeripheralSimulation();
         simulationCore = new SimulationCore(model, this::updateTable);
         
		    Thread simulationThread = new Thread(() -> {
		        try {
		            simulationCore.startSimulation(100); // Spustíme na 100 krokov
		            Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia dokončená."));
		        } catch (Exception e) {
		            e.printStackTrace();
		            Display.getDefault().asyncExec(() -> statusLabel.setText("Chyba počas simulácie."));
		        }
		    });

		    simulationThread.start();
		}
	 
	 private void updateTable(Double currentTime, String outputMessage) {
	        Display.getDefault().asyncExec(() -> {
	        	TableItem item = new TableItem(table, SWT.NONE);
	        	item.setText(new String[]{String.valueOf(currentTime), outputMessage});
	        });
	    }


	@Override
	public void setFocus() {
		table.setFocus();
	}
}
