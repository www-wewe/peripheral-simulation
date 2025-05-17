/** Copyright (c) 2025 Veronika Lenková */
package peripheralsimulation.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing a map of registers.
 * This class provides methods to read register values and check individual
 * bits.
 *
 * @author Veronika Lenková
 */
public class RegisterMap {

	/**
	 * Map representing the registers. Key = register offset, Value = register
	 * value.
	 */
	private Map<Integer, Integer> registerMap;

	/**
	 * Constructor for RegisterMap.
	 *
	 * @param registerMap A map of register offsets to their values.
	 */
	public RegisterMap(Map<Integer, Integer> registerMap) {
		this.registerMap = new HashMap<>(registerMap);
	}

	/**
	 * Returns the value of a register at the specified offset.
	 *
	 * @param addr The offset of the register.
	 * @return The value of the register. If the register does not exist, returns 0.
	 */
	public int getRegisterValue(int offset) {
		return registerMap.getOrDefault(offset, 0);
	}

	/**
	 * Sets the value of a register at the specified offset.
	 *
	 * @param offset The offset of the register.
	 * @param value  The value to set the register to.
	 */
	public void setRegisterValue(int offset, int value) {
		registerMap.put(offset, value);
	}

	/**
	 * Checks if the specified bit of a register is set.
	 *
	 * @param offset The offset of the register.
	 * @param bit    The bit position to check.
	 * @return True if the bit is set, false otherwise.
	 */
	public boolean isBitSet(int offset, int bit) {
		return ((getRegisterValue(offset) >> bit) & 1) == 1;
	}

	/**
	 * Checks if the register map contains a register at the specified offset.
	 *
	 * @param offset The offset of the register.
	 * @return True if the register exists, false otherwise.
	 */
	public boolean containsRegister(int offset) {
		return registerMap.containsKey(offset);
	}

}
