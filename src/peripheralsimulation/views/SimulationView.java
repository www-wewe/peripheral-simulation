package peripheralsimulation.views;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.SWT;
import jakarta.inject.Inject;
import peripheralsimulation.PeripheralSimulation;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;


import model.modeling.digraph;
import model.simulation.coordinator;
import GenCol.entity;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class SimulationView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "peripheralsimulation.views.SimulationView";

	@Inject IWorkbench workbench;
	
	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private Table table;
	 

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		@Override
		public Image getImage(Object obj) {
			return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		// Nastavenie layoutu
        parent.setLayout(new FillLayout(SWT.VERTICAL));

        // Štítok na stav simulácie
        Label statusLabel = new Label(parent, SWT.NONE);
        statusLabel.setText("Simulácia prebieha...");

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

        // Spustenie simulácie v samostatnom vlákne
        Thread simulationThread = new Thread(() -> {
            try {
                // Inicializácia modelu simulácie
                PeripheralSimulation model = new PeripheralSimulation();
                coordinator simulator = new coordinator(model);

                // Inicializácia simulátora
                simulator.initialize();

                // Simulácia krokov
                while (simulator.getTN() < Double.POSITIVE_INFINITY) { // Pokračuje, kým má simulácia udalosti
                    simulator.simulate(1); // Jeden časový krok

                    // Získanie aktuálneho času
                    double currentTime = simulator.getTN();

                    // Získanie výstupov z modelu
                    String outputMessage = model.getSimulationOutput();

                    // Aktualizácia tabuľky vo vlákne GUI
                    Display.getDefault().asyncExec(() -> {
                        if (outputMessage != null && !outputMessage.isEmpty()) {
                            TableItem item = new TableItem(table, SWT.NONE);
                            item.setText(new String[] { String.valueOf(currentTime), outputMessage });
                        }
                    });
                }

                // Simulácia dokončená
                Display.getDefault().asyncExec(() -> statusLabel.setText("Simulácia dokončená."));
            } catch (Exception e) {
                e.printStackTrace();
                Display.getDefault().asyncExec(() -> statusLabel.setText("Chyba počas simulácie."));
            }
        });

        simulationThread.start();
		
//		  viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//		  
//		  viewer.setContentProvider(ArrayContentProvider.getInstance());
//		  viewer.setInput(new String[] { "One", "Two", "Three" });
//		  viewer.setLabelProvider(new ViewLabelProvider());
		  
		  // Create the help context id for the viewer's control
		  workbench.getHelpSystem().setHelp(viewer.getControl(),
		  "PeripheralSimulation.viewer"); getSite().setSelectionProvider(viewer);
		  makeActions(); hookContextMenu(); hookDoubleClickAction();
		  contributeToActionBars();
		 
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SimulationView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(workbench.getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				IStructuredSelection selection = viewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Simulation View",
			message);
	}

	@Override
	public void setFocus() {
		table.setFocus();
	}
}
