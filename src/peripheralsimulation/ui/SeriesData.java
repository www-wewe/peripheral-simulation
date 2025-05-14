/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.extensions.charts.InteractiveChart;

/**
 * Data structure to hold series data for charting.
 *
 * @author Veronika Lenková
 */
public class SeriesData {
	/** The chart to which the series belongs */
	InteractiveChart chart;
	/** The series to be plotted */
	ILineSeries<?> series;
	/** Time values for the x-axis (seriex.setXseriex(timeValues) */
	List<Double> timeValues = new ArrayList<>();
	/** Output values for the y-axis (series.setYseriex(outputValues) */
	List<Double> outputValues = new ArrayList<>();
}
