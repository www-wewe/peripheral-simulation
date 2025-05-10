/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.io;

import java.util.List;
import peripheralsimulation.engine.UserEvent;

public class SimulationConfig {

	public UserPreferencesBlock preferences;
	public List<UserEvent> events;

	public SimulationConfig(UserPreferencesBlock preferences, List<UserEvent> events) {
		this.preferences = preferences;
		this.events = events;
	}

	public UserPreferencesBlock getPreferences() {
		return preferences;
	}

	public void setPreferences(UserPreferencesBlock preferences) {
		this.preferences = preferences;
	}

	public List<UserEvent> getEvents() {
		return events;
	}

	public void setEvents(List<UserEvent> events) {
		this.events = events;
	}

}