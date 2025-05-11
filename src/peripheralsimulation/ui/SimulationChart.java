/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.ui;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.swtchart.Range;
import org.eclipse.swtchart.extensions.charts.InteractiveChart;
import peripheralsimulation.io.UserPreferences;
import peripheralsimulation.utils.ColorsUtils;

import org.eclipse.swtchart.ILineSeries.PlotSymbolType;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeries.SeriesType;

/**
 * Class for displaying a simulation chart using SWT Chart.
 * 
 * This class implements the SimulationGUI interface and provides methods to
 * update and clear the chart.
 *
 * @author Veronika Lenková
 */
public class SimulationChart implements SimulationGUI {

	/** The chart in which the simulation results are displayed. */
	private InteractiveChart chart;
	/** User preferences for the simulation. */
	private UserPreferences userPreferences = UserPreferences.getInstance();;
	/** List of series data for charting. */
	private List<SeriesData> seriesList = new ArrayList<>();

	/**
	 * Constructor for the SimulationChart.
	 * 
	 * @param parent The parent composite for the chart.
	 */
	public SimulationChart(Composite parent) {
		chart = new InteractiveChart(parent, SWT.NONE);
		chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		chart.getTitle().setText("Simulation Chart");
		chart.getAxisSet().getXAxis(0).getTitle().setText("Time in " + userPreferences.getTimeUnits());
		chart.getAxisSet().getYAxis(0).getTitle().setText("Output Value");
		chart.getLegend().setVisible(false);

		// Set background color of the chart
		chart.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		chart.getPlotArea().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		chart.getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		chart.getAxisSet().getXAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		chart.getAxisSet().getYAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		chart.getLegend().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

		// axis tick color
		chart.getAxisSet().getXAxis(0).getTick()
				.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		chart.getAxisSet().getYAxis(0).getTick()
				.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));

		chart.getAxisSet().getXAxis(0).setRange(new Range(0, 10));
		chart.getAxisSet().getYAxis(0).setRange(new Range(0, 10));
	}

	@Override
	public void clear() {
		if (chart != null && !chart.isDisposed()) {
			for (SeriesData outputData : seriesList) {
				outputData.timeValues.clear();
				outputData.outputValues.clear();
				outputData.series.setXSeries(new double[0]);
				outputData.series.setYSeries(new double[0]);
			}
			seriesList.clear();
			for (ISeries<?> series : chart.getSeriesSet().getSeries()) {
				chart.getSeriesSet().deleteSeries(series.getId());
			}
			chart.getAxisSet().adjustRange();
			chart.redraw();
		}
	}

	@Override
	public void update(double timeValue, Object[] outputs) {
		// true if at least one output changed (for the onlyChanges mode)
		boolean anyChange = false;
		int[] selectedOutputsIndices = userPreferences.getSelectedOutputsIndices();
		// For each user-selected output:
		for (int position = 0;  position < selectedOutputsIndices.length; position++) {
			int outputIndex = selectedOutputsIndices[position];
			if (position >= seriesList.size()) {
				createSeriesForOutput(outputIndex);
			}
			SeriesData outputData = seriesList.get(position);
			Object outputValue = outputs[outputIndex];
			double numericVal = convertToDouble(outputValue);

			List<Double> outputValues = outputData.outputValues;
			Object lastValue = outputValues.isEmpty() ? null : outputValues.get(outputValues.size() - 1);
			if (lastValue == null || !lastValue.equals(numericVal)) {
				if (lastValue != null) {
					// Add the last value again (to make horizontal line)
					outputData.timeValues.add(timeValue);
					outputValues.add((double) lastValue);
				}
				// Add the new value
				outputData.timeValues.add(timeValue);
				outputValues.add(numericVal);
				anyChange = true;
				System.out.println("Time: " + timeValue);
				System.out.println("Output " + outputIndex + " changed to " + numericVal);
			}

		}

		// Only redraw if user doesn't want "only changes" or if at least one changed
		if (!userPreferences.isOnlyChanges() || anyChange) {
//			redrawAllSeries(); // Uncomment when you want to see "real-time" updates
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
	public void redrawAllSeries() {
		for (SeriesData outputData : seriesList) {
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
		chart.getLegend().setVisible(true);
	}

	/**
	 * Creates a new series for the given output.
	 * 
	 * @param outputIndex The index of the output for which to create the series.
	 */
	private void createSeriesForOutput(int outputIndex) {
		String outputName = userPreferences.getPeripheralModel().getOutputName(outputIndex);
		ILineSeries<?> lineSeries = (ILineSeries<?>) chart.getSeriesSet().createSeries(SeriesType.LINE, outputName);
		lineSeries.setLineStyle(LineStyle.SOLID);
		lineSeries.setSymbolType(PlotSymbolType.NONE);
		lineSeries.setLineColor(ColorsUtils.getNextColor());

		SeriesData outputData = new SeriesData();
		outputData.series = lineSeries;
		seriesList.add(outputData);
	}

	@Override
	public void setFocus() {
		chart.setFocus();
	}

	@Override
	public void onSelectedOutputsChanged() {
		Display.getDefault().syncExec(() -> {
			clear();
			int[] selectedOutputsIndices = userPreferences.getSelectedOutputsIndices();
			if (selectedOutputsIndices.length > seriesList.size()) {
				for (int outputIndex : selectedOutputsIndices) {
					createSeriesForOutput(outputIndex);
				}
			}
			chart.redraw();
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
		seriesList.clear();
	}

}
