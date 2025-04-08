package peripheralsimulation.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.swtchart.Range;

import peripheralsimulation.io.UserPreferences;

import org.eclipse.swtchart.ILineSeries.PlotSymbolType;
import org.eclipse.swtchart.ISeries.SeriesType;

public class SimulationChart implements SimulationGUI {

	/** The chart in which the simulation results are displayed. */
	private Chart chart;
	/** User preferences for the simulation. */
	private UserPreferences userPreferences = UserPreferences.getInstance();;
	/** Map of series data for charting. output name -> SeriesData */
	private Map<String, SeriesData> seriesMap = new HashMap<>();

	public SimulationChart(Composite parent) {
		chart = new Chart(parent, SWT.NONE);
		chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		chart.getTitle().setText("Simulation Chart");
		chart.getAxisSet().getXAxis(0).getTitle().setText("Time in microseconds");
		chart.getAxisSet().getYAxis(0).getTitle().setText("Output Value");
		chart.getLegend().setVisible(true);

		// Set background color of the chart
		chart.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		chart.getPlotArea().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		chart.getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		chart.getAxisSet().getXAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		chart.getAxisSet().getYAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		chart.getLegend().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

		// Initial axis range // TODO: according the user preferences
		chart.getAxisSet().getXAxis(0).setRange(new Range(0, 10));
		chart.getAxisSet().getYAxis(0).setRange(new Range(0, 10));
	}

	@Override
	public void clear() {
		if (chart != null && !chart.isDisposed()) {
			for (SeriesData outputData : seriesMap.values()) {
				outputData.timeValues.clear();
				outputData.outputValues.clear();
				outputData.series.setXSeries(new double[0]);
				outputData.series.setYSeries(new double[0]);
			}
			seriesMap.clear();
			chart.getAxisSet().adjustRange(); // TODO: Adjust axis range ?
			chart.redraw();
		}
	}

	/**
	 * Updates the chart with the latest data.
	 */
	@Override
	public void update(double timeValue, Map<String, Object> outputs) {
		// true if at least one output changed (for the onlyChanges mode)
		boolean anyChange = false;
		// For each user-selected output:
		for (String output : userPreferences.getSelectedOutputs()) {
			SeriesData outputData = seriesMap.get(output);
			if (outputData == null) {
				// We have not created a series for this output yet, do so
				createSeriesForOutput(output);
				outputData = seriesMap.get(output);
			}

			// Convert the 'Object' output to a numeric
			Object outputValue = outputs.get(output);
			double numericVal = convertToDouble(outputValue);

			List<Double> outputValues = outputData.outputValues;
			Object lastValue = outputValues.isEmpty() ? null
					: outputValues.get(outputValues.size() - 1);
			if (lastValue == null || !lastValue.equals(numericVal)) {
				// Output changed => add new data point for the new time
				outputData.timeValues.add(timeValue);
				outputValues.add(numericVal);
				anyChange = true;
				System.out.println("Time: " + timeValue);
				System.out.println("Output " + output + " changed to " + numericVal);
			}

		}

		// Only redraw if user doesn't want "only changes" or if at least one changed
		if (!userPreferences.isOnlyChanges() || anyChange) {
			redrawAllSeries();
		}
	}

	/**
	 * Convert the object into a double, handling booleans or parseable strings.
	 */
	private double convertToDouble(Object outputValue) {
		if (outputValue instanceof Number) {
			return ((Number) outputValue).doubleValue();
		} else if (outputValue instanceof Boolean) {
			if ((Boolean) outputValue) {
				return 1.0;
			} else {
				return 0.0;
			}
		} else {
			try {
				return Double.parseDouble(String.valueOf(outputValue));
			} catch (NumberFormatException e) {
				return 0.0;
			}
		}
	}

	/**
	 * Redraws all series in the chart.
	 */
	private void redrawAllSeries() {
		for (Map.Entry<String, SeriesData> entry : seriesMap.entrySet()) {
			SeriesData outputData = entry.getValue();

			// Convert the xVals and yVals to arrays
			double[] xs = new double[outputData.timeValues.size()];
			double[] ys = new double[outputData.outputValues.size()];
			for (int i = 0; i < outputData.timeValues.size(); i++) {
				xs[i] = outputData.timeValues.get(i);
				ys[i] = outputData.outputValues.get(i);
			}

			// Update the chart series
			outputData.series.setXSeries(xs);
			outputData.series.setYSeries(ys);
		}

		// Adjust axis range so we can see everything
		chart.getAxisSet().adjustRange();

		// Redraw chart
		chart.redraw();
	}

	/**
	 * Creates a new series for the given output.
	 * 
	 * @param output
	 */
	private void createSeriesForOutput(String outputName) {
		// create a new line series in the chart
		ILineSeries<?> lineSeries = (ILineSeries<?>) chart.getSeriesSet().createSeries(SeriesType.LINE, outputName);
		lineSeries.setLineStyle(LineStyle.SOLID);
		lineSeries.setSymbolType(PlotSymbolType.NONE);

		SeriesData outputData = new SeriesData();
		outputData.series = lineSeries;
		seriesMap.put(outputName, outputData);
	}

	@Override
	public void setFocus() {
		chart.setFocus();
	}

	@Override
	public void onSelectedOutputsChanged() {
		Display.getDefault().syncExec(() -> {
			// Clear the chart
			clear();

			// Recreate series for the selected outputs
//			for (String output : userPreferences.getSelectedOutputs()) {
//				if (!seriesMap.containsKey(output)) {
//					createSeriesForOutput(output);
//				}
//			}
			// Redraw the chart
			redrawAllSeries();
		});
	}

	@Override
	public Composite getParent() {
		return chart.getParent();
	}

	@Override
	public void dispose() {
		clear();
		if (chart != null) {
			chart.dispose();
		}
		seriesMap.clear();
	}

}
