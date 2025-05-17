/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * UserEventGenerator is responsible for scheduling user events in a simulation
 * engine. It maintains a list of user events and provides methods to add and
 * schedule them.
 *
 * @author Veronika Lenková
 */
public class UserEventGenerator {

	/**
	 * List of user events to be scheduled. Each event is defined by a UserEvent
	 * object.
	 */
	private List<UserEvent> userEvents = new ArrayList<>();

	/**
	 * Adds a user event definition to the list of user events.
	 * 
	 * @param eventDefinition The UserEvent object representing the event to be
	 *                        scheduled.
	 */
	public void addEvent(UserEvent eventDefinition) {
		userEvents.add(eventDefinition);
	}

	/**
	 * Called from SimulationEngine's initSimulation() to schedule these events.
	 */
	public void scheduleAll(SimulationEngine engine) {
		for (UserEvent event : userEvents) {
			scheduleOne(engine, event, event.getStartTime(), event.getRepeatCount());
		}
	}

	/**
	 * Schedules a single user event.
	 * 
	 * @param engine    The simulation engine to use for scheduling.
	 * @param event     The user event definition to schedule.
	 * @param eventTime The time at which the event should occur.
	 * @param remaining The number of times to repeat the event. If <= 0, it will
	 *                  repeat indefinitely.
	 */
	private void scheduleOne(SimulationEngine engine, UserEvent event, double eventTime, int remaining) {
		engine.scheduleEvent(eventTime, () -> {
			// apply the user event
			event.getTargetPeripheral().applyUserEvent(event);

			// if repeating, schedule again
			if (event.getPeriod() > 0) {
				int newCount = (remaining <= 0) ? remaining : remaining - 1;
				// if newCount=0 => infinite
				if (newCount == 0 || newCount > 0) {
					double nextTime = eventTime + event.getPeriod();
					// schedule the next
					scheduleOne(engine, event, nextTime, newCount);
				}
			}
		});
	}

	/**
	 * Clears all scheduled user events.
	 */
	public void clearEvents() {
		userEvents.clear();
	}

}
