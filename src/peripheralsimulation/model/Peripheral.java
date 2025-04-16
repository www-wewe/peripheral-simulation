package peripheralsimulation.model;

public enum Peripheral {

	SYSTICKTIMER("System Tick Timer"),
	COUNTER("Counter");

	private final String displayName;

	Peripheral(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
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

}
