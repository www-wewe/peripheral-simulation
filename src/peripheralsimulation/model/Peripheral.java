/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model;

/**
 * Enum representing different types of peripherals in the simulation. Each
 * peripheral has a display name that can be used for user-friendly
 * representation.
 *
 * @author Veronika Lenková
 */
public enum Peripheral {

	/** Peripheral representing a System Tick Timer. */
	SYSTICKTIMER("System Tick Timer"),

	/** Peripheral representing a simplyfied counter. */
	COUNTER("Counter"),

	/** Peripheral representing a Flexible I/O module. */
	FLEXIO("FlexIO");

	/** The display name of the peripheral. */
	private final String displayName;

	/**
	 * Constructor for Peripheral enum.
	 *
	 * @param displayName the display name of the peripheral
	 */
	Peripheral(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Lookup by display name.
	 *
	 * @param displayName the display name of the peripheral
	 * @return the corresponding Peripheral enum
	 * @throws IllegalArgumentException if no enum constant is found for the given
	 *                                  display name
	 */
	public static Peripheral fromDisplayName(String displayName) {
		for (Peripheral peripheralEnum : Peripheral.values()) {
			if (peripheralEnum.displayName.equals(displayName)) {
				return peripheralEnum;
			}
		}
		throw new IllegalArgumentException("No enum constant for display name: " + displayName);
	}

	@Override
	public String toString() {
		return displayName;
	}

}
