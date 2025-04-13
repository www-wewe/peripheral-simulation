package peripheralsimulation.engine;

import peripheralsimulation.model.PeripheralModel;

/**
 * Represents a user-defined event that can be scheduled in the simulation.
 * Contains information about the event's timing, target peripheral, action to
 * perform, and other relevant parameters.
 */
public class UserEvent {
	/**
	 * The time at which the first event fires.
	 */
	public double startTime;

	/**
	 * The period between subsequent events. If <= 0, it's a one-time event.
	 */
	public double period;

	/**
	 * The number of times to repeat. If 0 or negative => infinite.
	 */
	public int repeatCount;

	/**
	 * The peripheral we want to act on.
	 */
	public PeripheralModel targetPeripheral;

	/**
	 * The kind of action to perform: e.g. toggle bit, set bit, write register, etc.
	 */
	public UserEventType eventType;

	/**
	 * The address of the register to act on.
	 */
	public int registerAddress;

	/**
	 * The bit position to act on (toogle/set/clear). Ignored for write.
	 */
	public int bitPosition;

	/**
	 * The value to write to the register.
	 */
	public int value;

	public UserEvent(double startTime, double period, int repeatCount, PeripheralModel targetPeripheral,
			UserEventType eventType, int registerAddress, int bitPosition, int value) {
		this.startTime = startTime;
		this.period = period;
		this.repeatCount = repeatCount;
		this.targetPeripheral = targetPeripheral;
		this.eventType = eventType;
		this.registerAddress = registerAddress;
		this.bitPosition = bitPosition;
		this.value = value;
	}
}
