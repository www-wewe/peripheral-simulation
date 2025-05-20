/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.io;

import java.util.List;
import peripheralsimulation.engine.UserEvent;

/**
 * SimulationConfig class represents the settings for a simulation. It contains
 * user preferences and a list of events to be executed during the simulation.
 *
 * @author Veronika Lenková
 */
public class SimulationConfig {

	/** UserPreferences for the simulation. */
	public UserPreferencesBlock preferences;

	/** List of events to be executed during the simulation. */
	public List<UserEvent> events;

	/**
	 * Default constructor.
	 *
	 * @param preferences UserPreferencesBlock containing user preferences.
	 * @param events      List of UserEvent objects.
	 */
	public SimulationConfig(UserPreferencesBlock preferences, List<UserEvent> events) {
		this.preferences = preferences;
		this.events = events;
	}

	/**
	 * Get the user preferences for the simulation.
	 *
	 * @return UserPreferencesBlock containing user preferences.
	 */
	public UserPreferencesBlock getPreferences() {
		return preferences;
	}

	/**
	 * Get the list of events to be executed during the simulation.
	 *
	 * @return List of UserEvent objects.
	 */
	public List<UserEvent> getEvents() {
		return events;
	}

}