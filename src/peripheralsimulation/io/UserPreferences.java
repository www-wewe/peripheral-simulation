package peripheralsimulation.io;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import peripheralsimulation.model.PeripheralModel;
import peripheralsimulation.views.SimulationGuiChoice;

public final class UserPreferences {


	private static UserPreferences instance;
	private static final double MICROSECONDS_SCALE = 1e6;
	private static final double MS_SCALE = 1e3;
	private final DecimalFormat TIME_FORMAT = new DecimalFormat("#.######");

	private Set<String> selectedOutputs = new HashSet<>();
	private PeripheralModel peripheralModel;
	private List<UserPreferencesListener> listeners = new ArrayList<>();
	private boolean onlyChanges = false;
	private long millisToWait = 0;
	private SimulationGuiChoice selectedSimulationGUI = SimulationGuiChoice.TABLE;
	/**
	 * The frequency at which the simulation should be monitored. This is used to
	 * determine how often the simulation should update the GUI (e.g. 0.5 means show
	 * a table row every 0.5 simulation time)
	 */
	private double monitoringFreq;
	/** The range (from, to) of the simulation time */
	private double simulationTimeRangeFrom;
	private double simulationTimeRangeTo;
	private double timeScaleFactor = MICROSECONDS_SCALE; // e.g. scale from seconds to microseconds
	private String timeUnits = "us";

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

	public Set<String> getSelectedOutputs() {
		return selectedOutputs;
	}

	public void setSelectedOutputs(Set<String> selectedOutputs) {
		if (this.selectedOutputs.equals(selectedOutputs)) {
			return;
		}
		this.selectedOutputs = selectedOutputs;
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

	public double getMonitoringFreq() {
		return monitoringFreq;
	}

	public void setMonitoringFreq(double monitoringFreq) {
		this.monitoringFreq = monitoringFreq;
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
