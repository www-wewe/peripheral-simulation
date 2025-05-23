/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.io;

import java.util.List;

import peripheralsimulation.ui.SimulationGuiChoice;

/**
 * User preferences block in the YAML configuration file.
 *
 * This class represents the user preferences for the simulation, including
 * monitoring period, range, clock frequencies, wait time, output devices,
 * simulation GUI choice, and time unit.
 *
 * @author Veronika Lenková
 */
public class UserPreferencesBlock {
	/** The monitoring period in seconds. */
	private double monitoringPeriod;

	/** The range from value. */
	private double rangeFrom;

	/** The range to value. */
	private double rangeTo;

	/** The clock frequency. */
	private int clkFreq;

	/** The external clock frequency. */
	private int extClkFreq;

	/** The wait time in milliseconds. */
	private long waitMs;

	/** Whether to only show changes in the output. */
	private boolean onlyChanges;

	/** The list of outputs to show. */
	private List<String> outputs;

	/** The selected simulation GUI choice. */
	private SimulationGuiChoice simulationGui;

	/** The time unit for the simulation. */
	private String timeUnit;

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
	public UserPreferencesBlock(double monitoringPeriod, double rangeFrom, double rangeTo, int clkFreq, int extClkFreq,
			long waitMs, boolean onlyChanges, List<String> outputs, SimulationGuiChoice simulationGui,
			String timeUnit) {
		this.monitoringPeriod = monitoringPeriod;
		this.rangeFrom = rangeFrom;
		this.rangeTo = rangeTo;
		this.clkFreq = clkFreq;
		this.extClkFreq = extClkFreq;
		this.waitMs = waitMs;
		this.onlyChanges = onlyChanges;
		this.outputs = outputs;
		this.simulationGui = simulationGui;
		this.timeUnit = timeUnit;
	}

	public double getMonitoringPeriod() {
		return monitoringPeriod;
	}

	public void setMonitoringPeriod(double monitoringPeriod) {
		this.monitoringPeriod = monitoringPeriod;
	}

	public double getRangeFrom() {
		return rangeFrom;
	}

	public void setRangeFrom(double rangeFrom) {
		this.rangeFrom = rangeFrom;
	}

	public double getRangeTo() {
		return rangeTo;
	}

	public void setRangeTo(double rangeTo) {
		this.rangeTo = rangeTo;
	}

	public int getClkFreq() {
		return clkFreq;
	}

	public void setClkFreq(int clkFreq) {
		this.clkFreq = clkFreq;
	}

	public int getExtClkFreq() {
		return extClkFreq;
	}

	public void setExtClkFreq(int extClkFreq) {
		this.extClkFreq = extClkFreq;
	}

	public long getWaitMs() {
		return waitMs;
	}

	public void setWaitMs(long waitMs) {
		this.waitMs = waitMs;
	}

	public boolean isOnlyChanges() {
		return onlyChanges;
	}

	public void setOnlyChanges(boolean onlyChanges) {
		this.onlyChanges = onlyChanges;
	}

	public List<String> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<String> outputs) {
		this.outputs = outputs;
	}

	public SimulationGuiChoice getSimulationGui() {
		return simulationGui;
	}

	public void setSimulationGui(SimulationGuiChoice simulationGui) {
		this.simulationGui = simulationGui;
	}

	public String getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}

}
