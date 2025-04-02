package peripheralsimulation.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import peripheralsimulation.model.PeripheralModel;

public class UserPreferences {

	private double updateInterval;
	private Set<String> selectedOutputs;
	private PeripheralModel peripheralModel;
	private List<UserPreferencesListener> listeners = new ArrayList<>();
	private boolean onlyChanges = false;
	private static long millisToWait = 0;

	public void addListener(UserPreferencesListener listener) {
		listeners.add(listener);
	}

	public void removeListener(UserPreferencesListener listener) {
		listeners.remove(listener);
	}

	private void notifyListeners() {
		for (UserPreferencesListener listener : listeners) {
			listener.onPreferencesChanged();
		}
	}

	public double getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(double updateInterval) {
		this.updateInterval = updateInterval;
	}

	public Set<String> getSelectedOutputs() {
		return selectedOutputs;
	}

	public void setSelectedOutputs(Set<String> selectedOutputs) {
		this.selectedOutputs = selectedOutputs;
		notifyListeners();
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

	public static long getMillisToWait() {
		return millisToWait;
	}

	public static void setMillisToWait(long millis) {
		millisToWait = millis;
	}
}
