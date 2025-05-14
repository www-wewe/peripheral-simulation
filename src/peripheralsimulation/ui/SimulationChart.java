/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.ui;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

	/** Container that holds all individual charts. */
	private final Composite container;
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
		// Create a dedicated container so that we can add/remove charts freely
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
	}

	@Override
	public void clear() {
		for (SeriesData outputData : seriesList) {
			outputData.timeValues.clear();
			outputData.outputValues.clear();
			outputData.series.setXSeries(new double[0]);
			outputData.series.setYSeries(new double[0]);
			for (ISeries<?> series : outputData.chart.getSeriesSet().getSeries()) {
				outputData.chart.getSeriesSet().deleteSeries(series.getId());
				outputData.chart.dispose();
			}
		}
		seriesList.clear();
		synchroniseTimeAxes();
		container.layout();
	}

	@Override
	public void update(double timeValue, Object[] outputs) {
		int[] selectedOutputs = userPreferences.getSelectedOutputsIndices();
		// Ensure we have a chart for every selected output
		ensureChartCount(selectedOutputs.length);
		for (int position = 0; position < selectedOutputs.length; position++) {
			int outputIndex = selectedOutputs[position];
			SeriesData outputData = seriesList.get(position);
			double numericVal = convertToDouble(outputs[outputIndex]);
			List<Double> outputValues = outputData.outputValues;
			Double lastVal = outputValues.isEmpty() ? null : outputValues.get(outputValues.size() - 1);

			if (lastVal == null || !lastVal.equals(numericVal)) {
				if (lastVal != null) {
					// Horizontal segment up to the change
					outputData.timeValues.add(timeValue);
					outputValues.add(lastVal);
				}
				outputData.timeValues.add(timeValue);
				outputValues.add(numericVal);
			}
		}
		// redrawAllSeries(); // Uncomment when you want to see "real-time" updates
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
			int size = outputData.timeValues.size();
			double[] xs = new double[size];
			double[] ys = new double[size];
			for (int i = 0; i < size; i++) {
				xs[i] = outputData.timeValues.get(i);
				ys[i] = outputData.outputValues.get(i);
			}
			outputData.series.setXSeries(xs);
			outputData.series.setYSeries(ys);
		}
		synchroniseTimeAxes();
		container.layout();
	}

	/**
	 * Creates a new chart for the specified output.
	 * 
	 * @param logicalPosition The logical position of the output in the chart.
	 */
	private void createChartForOutput(int logicalPosition) {
		int outputIndex = userPreferences.getSelectedOutputsIndices()[logicalPosition];
		String outputName = userPreferences.getPeripheralModel().getOutputName(outputIndex);

		SeriesData data = new SeriesData();
		data.chart = new InteractiveChart(container, SWT.NONE);
		data.chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		// Cosmetic setup ---------------------------------------------------
		data.chart.getTitle().setText(outputName);
		data.chart.getLegend().setVisible(false);
		data.chart.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		data.chart.getPlotArea().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		// Axis titles / appearance ----------------------------------------
		data.chart.getAxisSet().getXAxis(0).getTitle().setText("Time in " + userPreferences.getTimeUnits());
		data.chart.getAxisSet().getYAxis(0).getTitle().setVisible(false);

		// Set background color of the chart
		data.chart.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		data.chart.getPlotArea().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		data.chart.getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		data.chart.getAxisSet().getXAxis(0).getTitle()
				.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		data.chart.getAxisSet().getYAxis(0).getTitle()
				.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		data.chart.getLegend().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

		// axis tick color
		data.chart.getAxisSet().getXAxis(0).getTick()
				.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		data.chart.getAxisSet().getYAxis(0).getTick()
				.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));

		data.chart.getAxisSet().getXAxis(0).setRange(new Range(0, 10));
		data.chart.getAxisSet().getYAxis(0).setRange(new Range(0, 10));

		// Create the line series ------------------------------------------
		data.series = (ILineSeries<?>) data.chart.getSeriesSet().createSeries(SeriesType.LINE, outputName);

		data.series.setLineStyle(LineStyle.SOLID);
		data.series.setSymbolType(PlotSymbolType.NONE);
		data.series.setLineColor(ColorsUtils.getNextColor());

		seriesList.add(data);
	}

	/**
	 * Synchronizes the time axes of all charts to have the same range.
	 */
	private void synchroniseTimeAxes() {
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (SeriesData outputData : seriesList) {
			if (!outputData.timeValues.isEmpty()) {
				min = Math.min(min, outputData.timeValues.get(0));
				max = Math.max(max, outputData.timeValues.get(outputData.timeValues.size() - 1));
			}
		}
		if (min == Double.MAX_VALUE || max == -Double.MAX_VALUE) {
			min = 0;
			max = 1;
		}
		if (min == max) {
			max = min + 1;
		}
		Range commonRange = new Range(min, max);
		for (SeriesData outputData : seriesList) {
			outputData.chart.getAxisSet().getXAxis(0).setRange(commonRange);
			outputData.chart.getAxisSet().getYAxis(0).adjustRange();
		}
	}

	/**
	 * Ensures that the number of charts matches the required number.
	 * 
	 * @param required The required number of charts.
	 */
	private void ensureChartCount(int required) {
		// Create missing charts
		while (seriesList.size() < required) {
			createChartForOutput(seriesList.size());
		}
		// Dispose surplus charts (e.g. when user deselects outputs)
		while (seriesList.size() > required) {
			SeriesData removed = seriesList.remove(seriesList.size() - 1);
			if (!removed.chart.isDisposed()) {
				removed.chart.dispose();
			}
		}
	}

	@Override
	public void setFocus() {
		container.setFocus();
	}

	@Override
	public void onSelectedOutputsChanged() {
		Display.getDefault().syncExec(() -> {
			clear();
			ensureChartCount(userPreferences.getSelectedOutputsIndices().length);
			container.layout();
		});
	}

	@Override
	public Composite getParent() {
		return container.getParent();
	}

	@Override
	public void dispose() {
		clear();
		for (SeriesData d : seriesList) {
			if (d.chart != null && !d.chart.isDisposed()) {
				d.chart.dispose();
			}
		}
		if (container != null) {
			container.dispose();
		}
		seriesList.clear();
	}

}
