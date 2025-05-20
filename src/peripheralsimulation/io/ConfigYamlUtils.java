/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import peripheralsimulation.engine.UserEvent;
import peripheralsimulation.engine.UserEventType;
import peripheralsimulation.ui.SimulationGuiChoice;

/**
 * ConfigYamlUtils class is responsible for loading and saving YAML
 * configuration files for the simulation.
 *
 * @author Veronika Lenková
 */
public final class ConfigYamlUtils {

	/**
	 * Load a YAML configuration file.
	 *
	 * @param file The path to the configuration file.
	 * @return The loaded SimulationConfig object.
	 * @throws IOException If an I/O error occurs while reading the file.
	 */
	@SuppressWarnings("unchecked")
	public static SimulationConfig loadYaml(Path file) throws IOException {
		Yaml yaml = new Yaml();
		try (InputStream in = Files.newInputStream(file)) {
			Object cfg = yaml.load(in);
			if (cfg == null) {
				throw new IOException("YAML is empty or does not match SimulationConfig.");
			}
			return parseYamlToConfig((Map<String, Object>) cfg);
		}
	}

	/**
	 * Save a YAML configuration file.
	 *
	 * @param file The path to the configuration file.
	 * @param cfg  The SimulationConfig object to save.
	 * @throws IOException If an I/O error occurs while writing the file.
	 */
	public static void saveYaml(Path file, SimulationConfig cfg) throws IOException {
		DumperOptions o = new DumperOptions();
		o.setIndent(2);
		o.setPrettyFlow(true);
		Yaml yaml = new Yaml(o);
		try (BufferedWriter w = Files.newBufferedWriter(file)) {
			yaml.dump(cfg, w);
		}
	}

	/**
	 * Convert a YAML configuration file to a SimulationConfig object.
	 *
	 * @param root The root map of the YAML configuration.
	 * @return The SimulationConfig object.
	 */
	@SuppressWarnings("unchecked")
	public static SimulationConfig parseYamlToConfig(Map<String, Object> root) {

		/* ---------- 1. preferences ---------- */
		Map<String, Object> preferencesMap = (Map<String, Object>) root.getOrDefault("preferences", Map.of());

		double monitoringPeriod = toDouble(preferencesMap.get("monitoringPeriod"), 0);
		double rangeFrom = toDouble(preferencesMap.get("rangeFrom"), 0);
		double rangeTo = toDouble(preferencesMap.get("rangeTo"), 0);
		int clkFreq = toInt(preferencesMap.get("clkFreq"), 0);
		int extClkFreq = toInt(preferencesMap.get("extClkFreq"), 0);
		long waitMs = toLong(preferencesMap.get("waitMs"), 0);
		boolean onlyChanges = toBoolean(preferencesMap.get("onlyChanges"), false);
		List<String> outputs = (List<String>) preferencesMap.getOrDefault("outputs", List.of());
		SimulationGuiChoice gui = SimulationGuiChoice
				.valueOf(String.valueOf(preferencesMap.getOrDefault("gui", "TABLE")).toUpperCase());
		String timeUnit = String.valueOf(preferencesMap.getOrDefault("timeUnit", "ms"));

		UserPreferencesBlock prefBlock = new UserPreferencesBlock(monitoringPeriod, rangeFrom, rangeTo, clkFreq,
				extClkFreq, waitMs, onlyChanges, outputs, gui, timeUnit);

		/* ---------- 2. events ---------- */
		List<Map<String, Object>> eventsList = (List<Map<String, Object>>) root.getOrDefault("events", List.of());

		List<UserEvent> events = new ArrayList<>();
		for (Map<String, Object> event : eventsList) {
			double start = toDouble(event.get("start"), 0);
			double period = toDouble(event.get("period"), 0);
			int repeat = toInt(event.get("repeat"), 1);
			UserEventType type = UserEventType.valueOf(String.valueOf(event.get("type")).toUpperCase());
			int reg = toHexOrDec(event.get("reg"), 0);
			int bit = toInt(event.get("bit"), 0);
			int value = toInt(event.get("value"), 0);

			events.add(new UserEvent(start, period, repeat, UserPreferences.getInstance().getPeripheralModel(), type,
					reg, bit, value));
		}

		return new SimulationConfig(prefBlock, events);
	}

	/* ======= helpers functions ======= */

	/**
	 * Helper function to convert an Object to a double value.
	 *
	 * @param o   The object to convert.
	 * @param def The default value to return if conversion fails.
	 * @return The converted double value.
	 */
	private static double toDouble(Object o, double def) {
		return (o instanceof Number n) ? n.doubleValue() : (o != null) ? Double.parseDouble(o.toString()) : def;
	}

	/**
	 * Helper function to convert an Object to an int value.
	 *
	 * @param o   The object to convert.
	 * @param def The default value to return if conversion fails.
	 * @return The converted int value.
	 */
	private static int toInt(Object o, int def) {
		return (o instanceof Number n) ? n.intValue() : (o != null) ? Integer.parseInt(o.toString()) : def;
	}

	/**
	 * Helper function to convert an Object to a long value.
	 *
	 * @param o   The object to convert.
	 * @param def The default value to return if conversion fails.
	 * @return The converted long value.
	 */
	private static long toLong(Object o, long def) {
		return (o instanceof Number n) ? n.longValue() : (o != null) ? Long.parseLong(o.toString()) : def;
	}

	/**
	 * Helper function to convert an Object to a boolean value.
	 *
	 * @param o   The object to convert.
	 * @param def The default value to return if conversion fails.
	 * @return The converted boolean value.
	 */
	private static boolean toBoolean(Object o, boolean def) {
		return (o instanceof Boolean b) ? b : (o != null) ? Boolean.parseBoolean(o.toString()) : def;
	}

	/**
	 * Helper function to convert an Object to a hexadecimal or decimal int value.
	 *
	 * @param o   The object to convert.
	 * @param def The default value to return if conversion fails.
	 * @return The converted int value.
	 */
	private static int toHexOrDec(Object o, int def) {
		try {
			if (o instanceof String stringToParse) {
				if (stringToParse.startsWith("0x") || stringToParse.startsWith("0X")) {
					return Integer.parseInt(stringToParse.substring(2), 16);
				}
			}
			return toInt(o, def);
		} catch (NumberFormatException e) {
			return def;
		}
	}

}
