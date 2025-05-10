/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import peripheralsimulation.model.flexio.FlexIOConfig;
import peripheralsimulation.model.systick.SysTickTimerConfig;

/**
 * Utility class for register-related operations.
 * 
 * This class provides constants and methods for working with registers in the
 * simulation.
 * 
 * @author Veronika Lenková
 */
public class RegisterUtils {

	/** 24-bit register mask */
	public static final int BIT_MASK = 0x00FFFFFF;

	/**
	 * Loads FlexIO registers from a CSV file and returns them as a map (register
	 * name -> register value).
	 *
	 * @return Map of register names and their values.
	 */
	public static Map<String, Integer> loadRegistersFromCsv() {

		Map<String, Integer> map = new HashMap<>();

		FileDialog importDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
		importDialog.setText("Import registers from CSV file");
		importDialog.setFilterExtensions(new String[] { "*.csv", "*.*" });

		String file = importDialog.open();
		if (file == null) { // user canceled the dialog
			return map; // empty map
		}

		try {
			Files.lines(Path.of(file)).filter(line -> !line.isBlank()).forEach(line -> {
				String[] parts = line.split(",", 2);
				if (parts.length != 2)
					return;
				String regName = parts[0].trim();
				String hexValue = parts[1].trim().replace("0x", "");
				int regValue = (int) Long.parseLong(hexValue, 16);
				map.put(regName, regValue);
			});
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Import Successful",
					"The registers were successfully imported.");

		} catch (IOException | NumberFormatException ex) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error during import",
					"CSV file could not be loaded.\n" + ex.getMessage());
		}

		return map;
	}

	/**
	 * Converts a map of FlexIO register names and values to a RegisterMap object.
	 *
	 * @param map Map of register names and their values.
	 * @return RegisterMap object containing the converted registers.
	 */
	public static RegisterMap convertToFlexIORegisterMap(Map<String, Integer> map) {
		Map<Integer, Integer> registerMap = new HashMap<>();
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			int offset = FlexIOConfig.getRegisterOffset(entry.getKey());
			if (offset != -1) {
				registerMap.put(offset, entry.getValue());
			}
		}
		return new RegisterMap(registerMap);
	}

	/**
	 * Converts a map of SysTick register names and values to a RegisterMap object.
	 *
	 * @param map Map of register names and their values.
	 * @return RegisterMap object containing the converted registers.
	 */
	public static RegisterMap convertToSysTickRegisterMap(Map<String,Integer> map){
	    Map<Integer,Integer> registerMap = new HashMap<>();
	    for (Map.Entry<String, Integer> entry : map.entrySet()) {
	        int offset = SysTickTimerConfig.getRegisterOffset(entry.getKey());
	        if (offset != -1) {
				registerMap.put(offset, entry.getValue());
			}
		}
	    return new RegisterMap(registerMap);
	}


}
