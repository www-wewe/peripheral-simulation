/** Copyright (c) 2025 Veronika Lenková */
package peripheralsimulation.utils;

import java.util.Map;

/**
 * Class representing a map of registers.
 * 
 * This class provides methods to read register values and check individual
 * bits.
 * 
 * @author Veronika Lenková
 */
public class RegisterMap {

	/**
	 * Map representing the registers. Key = register address, Value = register value.
	 */
	private Map<Integer, Integer> registerMap;

	/**
	 * Constructor for RegisterMap.
	 * 
	 * @param registerMap A map of register addresses to their values.
	 */
	public RegisterMap(Map<Integer, Integer> registerMap) {
		this.registerMap = registerMap;
	}

	/**
	 * Returns the value of a register at the specified address.
	 * 
	 * @param addr The address of the register.
	 * @return The value of the register. If the register does not exist, returns 0.
	 */
	public int getRegisterValue(int address) {
        return registerMap.getOrDefault(address, 0);
    }

	/**
	 * Sets the value of a register at the specified address.
	 * @param address The address of the register.
	 * @param value The value to set the register to.
	 */
	public void setRegisterValue(int address, int value) {
		registerMap.put(address, value);
	}

	/**
	 * Checks if the specified bit of a register is set.
	 * @param address The address of the register.
	 * @param bit The bit position to check.
	 * @return True if the bit is set, false otherwise.
	 */
    public boolean isBitSet(int address, int bit) {
        return ((getRegisterValue(address) >> bit) & 1) == 1;
    }

}
