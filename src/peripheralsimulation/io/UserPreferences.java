package peripheralsimulation.io;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import peripheralsimulation.model.PeripheralModel;
import peripheralsimulation.ui.SimulationGuiChoice;

public final class UserPreferences {

	/** Singleton instance of UserPreferences */
	private static UserPreferences instance;

	/** Time format for displaying time values */
	private final DecimalFormat TIME_FORMAT = new DecimalFormat("#.######");

	/** The list of selected outputs to be monitored */
	private List<String> selectedOutputs = new ArrayList<>();

	/** The indices of the selected outputs */
	private int[] selectedOutputsIndices = new int[0];

	/** The peripheral model associated with the user preferences */
	private PeripheralModel peripheralModel;

	/** The list of listeners for user preferences changes */
	private List<UserPreferencesListener> listeners = new ArrayList<>();

	/** The flag indicating whether to show only changes in the output */
	private boolean onlyChanges = false;

	/** The time to wait in milliseconds (used with sleep for simulation pauses) */
	private long millisToWait = 0;

	/** The selected simulation GUI choice */
	private SimulationGuiChoice selectedSimulationGUI = SimulationGuiChoice.TABLE;

	/** The monitoring period in seconds */
	private double monitoringPeriod;

	/** The simulation time range from which to start monitoring */
	private double simulationTimeRangeFrom;

	/** The simulation time range to which to stop monitoring */
	private double simulationTimeRangeTo;

	/** Constant value for converting time to microseconds */
	private static final double MICROSECONDS_SCALE = 1e6;

	/** Constant value for converting time to milliseconds */
	private static final double MS_SCALE = 1e3;

	/** The time scale factor for converting simulation time to display time */
	private double timeScaleFactor = MS_SCALE;

	/** The time units for displaying time values */
	private String timeUnits = "ms";

	private UserPreferences() {
		// Private constructor to prevent instantiation
	}

	public void addListener(UserPreferencesListener listener) {
		listeners.add(listener);
	}

	public void removeListener(UserPreferencesListener listener) {
		listeners.remove(listener);
	}

	private void notifyListenersWhenSelectedOutputsChanged() {
		for (UserPreferencesListener listener : listeners) {
			listener.onSelectedOutputsChanged();
		}
	}

	private void notifyListenersWhenSelectedSimulationGUIChanged() {
		for (UserPreferencesListener listener : listeners) {
			listener.onSelectedSimulationGUIChanged();
		}
	}

	public List<String> getSelectedOutputs() {
		return selectedOutputs;
	}

	public void setSelectedOutputs(List<String> selectedOutputs) {
		if (this.selectedOutputs.equals(selectedOutputs)) {
			return;
		}
		this.selectedOutputs = selectedOutputs;
		notifyListenersWhenSelectedOutputsChanged();
	}

	public int[] getSelectedOutputsIndices() {
		return selectedOutputsIndices;
	}

	public void setSelectedOutputsIndices(int[] selectedOutputsIndices) {
		this.selectedOutputsIndices = selectedOutputsIndices;
		notifyListenersWhenSelectedOutputsChanged();
	}

	public PeripheralModel getPeripheralModel() {
		return peripheralModel;
	}

	public void setPeripheralModel(PeripheralModel peripheralModel) {
		this.peripheralModel = peripheralModel;
	}

	public boolean isOnlyChanges() {
		return onlyChanges;
	}

	public void setOnlyChanges(boolean onlyChanges) {
		this.onlyChanges = onlyChanges;
	}

	public long getMillisToWait() {
		return millisToWait;
	}

	public void setMillisToWait(long millis) {
		millisToWait = millis;
	}

	public SimulationGuiChoice getSelectedSimulationGUI() {
		return selectedSimulationGUI;
	}

	public void setSelectedSimulationGUI(SimulationGuiChoice selectedSimulationGUI) {
		if (this.selectedSimulationGUI == selectedSimulationGUI) {
			return;
		}
		this.selectedSimulationGUI = selectedSimulationGUI;
		notifyListenersWhenSelectedSimulationGUIChanged();
	}

	public double getMonitoringPeriod() {
		return monitoringPeriod;
	}

	public void setMonitoringPeriod(double monitoringPeriod) {
		this.monitoringPeriod = monitoringPeriod;
	}

	public double getSimulationTimeRangeFrom() {
		return simulationTimeRangeFrom;
	}

	public void setSimulationTimeRangeFrom(double simulationTimeRangeFrom) {
		this.simulationTimeRangeFrom = simulationTimeRangeFrom;
	}

	public double getSimulationTimeRangeTo() {
		return simulationTimeRangeTo;
	}

	public void setSimulationTimeRangeTo(double simulationTimeRangeTo) {
		this.simulationTimeRangeTo = simulationTimeRangeTo;
	}

	public double getTimeScaleFactor() {
		return timeScaleFactor;
	}

	public void setTimeScaleFactor(double timeScaleFactor) {
		this.timeScaleFactor = timeScaleFactor;
	}

	public String getTimeUnits() {
		return timeUnits;
	}

	public void setTimeUnits(String timeUnits) {
		this.timeUnits = timeUnits;
	}

	public DecimalFormat getTimeFormat() {
		return TIME_FORMAT;
	}

	public static UserPreferences getInstance() {
		if (instance == null) {
			instance = new UserPreferences();
		}
		return instance;

	}
}
