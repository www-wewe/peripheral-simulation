/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import peripheralsimulation.engine.UserEvent;
import peripheralsimulation.ui.SimulationGuiChoice;

/**
 * ConfigIO is responsible for loading and saving YAML configuration files for
 * the simulation.
 *
 * @author Veronika Lenková
 */
public final class ConfigIO {

	/**
	 * Load a YAML configuration file.
	 *
	 * @param file The path to the configuration file.
	 * @return The loaded SimulationConfig object.
	 * @throws IOException If an I/O error occurs while reading the file.
	 */
	public static SimulationConfig loadYaml(String file) throws IOException {
		Load loader = new Load(LoadSettings.builder().build());
		return (SimulationConfig) loader.loadFromString(file);
	}

	/**
	 * Save a YAML configuration file.
	 *
	 * @param file The path to the configuration file.
	 * @param cfg  The SimulationConfig object to save.
	 * @throws IOException If an I/O error occurs while writing the file.
	 */
	public static void saveYaml(Path file, SimulationConfig cfg) throws IOException {
		Dump dumper = new Dump(DumpSettings.builder().build());
		String out = dumper.dumpToString(cfg);
		try (var outStream = Files.newOutputStream(file)) {
			outStream.write(out.getBytes());
		}
	}

	/**
	 * Simulation configuration object.
	 *
	 * @param preferences The user preferences block.
	 * @param events      The list of user events.
	 */
	public record SimulationConfig(
			UserPreferencesBlock preferences,
			List<UserEvent> events) {
	}

	/**
	 * User preferences block in the YAML configuration file.
	 *
	 * @param monitoringPeriod The monitoring period in seconds.
	 * @param rangeFrom        The range from value.
	 * @param rangeTo          The range to value.
	 * @param clkFreq          The clock frequency.
	 * @param extClkFreq       The external clock frequency.
	 * @param waitMs           The wait time in milliseconds.
	 * @param onlyChanges      Whether to only show changes.
	 * @param outputs          The list of output devices.
	 * @param simulationGui    The simulation GUI choice.
	 * @param timeUnit         The time unit for the simulation.
	 */
	public record UserPreferencesBlock(
			double monitoringPeriod,
	        double rangeFrom,
			double rangeTo,
			int clkFreq,
	        int extClkFreq,
	        long waitMs,
	        boolean onlyChanges,
	        List<String> outputs,
	        SimulationGuiChoice simulationGui,
	        String timeUnit
	) {}

}
