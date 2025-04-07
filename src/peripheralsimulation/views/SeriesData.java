package peripheralsimulation.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtchart.ILineSeries;

/**
 * Data structure to hold series data for charting.
 */
public class SeriesData {
	ILineSeries<?> series;
	List<Double> timeValues = new ArrayList<>();
	List<Double> outputValues = new ArrayList<>();
}
