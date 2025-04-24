/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.engine;

import peripheralsimulation.model.PeripheralModel;

/**
 * Represents a user-defined event that can be scheduled in the simulation.
 * Contains information about the event's timing, target peripheral, action to
 * perform, and other relevant parameters.
 *
 * @author Veronika Lenková
 */
public class UserEvent {
	/**
	 * The time at which the first event fires.
	 */
	private double startTime;

	/**
	 * The period between subsequent events. If <= 0, it's a one-time event.
	 */
	private double period;

	/**
	 * The number of times to repeat. If 0 or negative => infinite.
	 */
	private int repeatCount;

	/**
	 * The peripheral we want to act on.
	 */
	private PeripheralModel targetPeripheral;

	/**
	 * The kind of action to perform: e.g. toggle bit, set bit, write register, etc.
	 */
	private UserEventType eventType;

	/**
	 * The address of the register to act on.
	 */
	private int registerAddress;

	/**
	 * The bit position to act on (toogle/set/clear). Ignored for write.
	 */
	private int bitPosition;

	/**
	 * The value to write to the register.
	 */
	private int value;

	/**
	 * Constructs a new UserEvent with the specified parameters.
	 *
	 * @param startTime        The time at which the first event fires.
	 * @param period           The period between subsequent events. If <= 0, it's a
	 *                         one-time event.
	 * @param repeatCount      The number of times to repeat. If 0 or negative =>
	 *                         infinite.
	 * @param targetPeripheral The peripheral we want to act on.
	 * @param eventType        The kind of action to perform: e.g. toggle bit, set
	 *                         bit, write register, etc.
	 * @param registerAddress  The address of the register to act on.
	 * @param bitPosition      The bit position to act on (toogle/set/clear).
	 *                         Ignored for write.
	 * @param value            The value to write to the register.
	 */
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

	/* ================================================================== */
	/*                     Getters and Setters                            */
	/* ================================================================== */

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public double getPeriod() {
		return period;
	}

	public void setPeriod(double period) {
		this.period = period;
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}

	public PeripheralModel getTargetPeripheral() {
		return targetPeripheral;
	}

	public void setTargetPeripheral(PeripheralModel targetPeripheral) {
		this.targetPeripheral = targetPeripheral;
	}

	public UserEventType getEventType() {
		return eventType;
	}

	public void setEventType(UserEventType eventType) {
		this.eventType = eventType;
	}

	public int getRegisterAddress() {
		return registerAddress;
	}

	public void setRegisterAddress(int registerAddress) {
		this.registerAddress = registerAddress;
	}

	public int getBitPosition() {
		return bitPosition;
	}

	public void setBitPosition(int bitPosition) {
		this.bitPosition = bitPosition;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
