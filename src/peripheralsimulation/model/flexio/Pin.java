/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.flexio;

public final class Pin {
	public boolean isOutput = false;
	public int level = 0; // 0 / 1
	public boolean invert = false; // PINPOL

	public int logicalLevel() {
		return invert ? (level ^ 0x1) : level;
	}

	public void clear() {
		isOutput = false;
		level = 0;
		invert = false;
	}
}
