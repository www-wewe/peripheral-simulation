package peripheralsimulation.io;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import peripheralsimulation.model.PeripheralModel;
import peripheralsimulation.ui.SimulationGuiChoice;

public final class UserPreferences {

	private static UserPreferences instance;
	private static final double MICROSECONDS_SCALE = 1e6;
	private static final double MS_SCALE = 1e3;
	private final DecimalFormat TIME_FORMAT = new DecimalFormat("#.######");

	private List<String> selectedOutputs = new ArrayList<>();
	private int[] selectedOutputsIndices = new int[0];
	private PeripheralModel peripheralModel;
	private List<UserPreferencesListener> listeners = new ArrayList<>();
	private boolean onlyChanges = false;
	private long millisToWait = 0;
	private SimulationGuiChoice selectedSimulationGUI = SimulationGuiChoice.TABLE;

	/**
	 * The frequency at which the simulation should be monitored. This is used to
	 * determine how often the simulation should update the GUI (e.g. 0.5 means show
	 * a table row every 0.5 seconds).
	 */
	private double monitoringPeriod;

	/** The range (from, to) of the simulation time */
	private double simulationTimeRangeFrom;
	private double simulationTimeRangeTo;
	private double timeScaleFactor = MS_SCALE; // e.g. scale from seconds to milliseconds
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

	public void setMonitoringFreq(double monitoringFreq) {
		this.monitoringPeriod = monitoringFreq;
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
