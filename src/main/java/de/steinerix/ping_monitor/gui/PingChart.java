package de.steinerix.ping_monitor.gui;

import de.steinerix.ping_monitor.PingResponse.Type;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;

class PingSeries {
	private final Type type;
	private final Series<Number, Number> series;

	PingSeries(Series<Number, Number> series, Type type) {
		this.series = series;
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public Series<Number, Number> getSeries() {
		return series;
	}
}

/**
 * A custom line chart for live ping representation
 * 
 * @author usr
 *
 */
public class PingChart extends LineChart<Number, Number> {
	private PingSeries lastModifiedSeries;
	private final int maxElements;
	private final double maxGraph;

	// assume 100ms interval pings for a year: 10 * 86400 * 365 << 2^63 - 1
	private long xPosition = 0;

	/**
	 * 
	 * @param xAxis
	 * @param yAxis
	 * @param maxElements
	 *            defines the number of displayed values
	 * @param maxGraph
	 *            defines the upper bound of y axis
	 */
	public PingChart(NumberAxis xAxis, NumberAxis yAxis, int maxElements,
			double maxGraph) {
		super(xAxis, yAxis);
		this.maxElements = maxElements;
		this.maxGraph = maxGraph;
		this.setCreateSymbols(false); // no symbols in graph
		this.setLegendVisible(false);

		// set axis behavior
		xAxis.setForceZeroInRange(false);
		xAxis.setAutoRanging(false);
		xAxis.setLowerBound(1);
		xAxis.setUpperBound(maxElements);

		yAxis.setAutoRanging(false);
		yAxis.setTickUnit(maxGraph / 5);
		yAxis.setLowerBound(0);
		yAxis.setUpperBound(maxGraph);
	}

	/**
	 * Add a ping response to the chart with time in ms. (If PingSeries.Type ==
	 * NOT_REACHABLE the time value should be set to 0)
	 */
	public void addPingResponse(Type type, double time) {
		if (time > maxGraph) { // cap values to upper bound
			time = maxGraph;
		}

		// may be extended in future releases
		switch (type) {
		case NORMAL: // intended fall through
		case LIMIT_EXCEEDED:
			addValue(type, time);
			break;
		case TIMEOUT: // intended fall through
		case NOT_REACHABLE: //
			if (lastModifiedSeries != null) {
				time = getLastValue().getYValue().doubleValue();
			}
			addValue(type, time);
			break;
		}
	}

	/**
	 * Returns the last modified series if PingSeries.Type matches or allocates
	 * a new series
	 */
	private Series<Number, Number> allocateSeries(Type type) {
		Series<Number, Number> series;

		if (lastModifiedSeries == null) {
			series = addSeries(type);
		} else if (lastModifiedSeries.getType() != type) {
			series = addSeries(type);

			// connect this series with last element
			Number x = getLastValue().getXValue();
			Number y = getLastValue().getYValue();
			series.getData().add(new Data<Number, Number>(x, y));
		} else {
			series = lastModifiedSeries.getSeries();
		}
		return series;
	}

	/** Adds a new series formatted according to Type type to ping chart */
	private Series<Number, Number> addSeries(Type type) {
		Series<Number, Number> series = new Series<Number, Number>();
		String seriesCssStyle = "";
		seriesCssStyle += "-fx-stroke-width: 1.5px;"; // line thickness

		this.getData().add(series); // adds series to chart

		if (type == Type.NORMAL) {
			seriesCssStyle += "-fx-stroke: green; ";
		} else {
			seriesCssStyle += "-fx-stroke: red; ";
			if (type == Type.NOT_REACHABLE || type == Type.TIMEOUT) {
				// dotted line
				seriesCssStyle += ("-fx-stroke-dash-array: 0.1 5.0; ");
			}
		}
		// node is only present after series has been added to chart
		series.getNode().setStyle(seriesCssStyle);

		return series;
	}

	/** Adds a new value */
	private void addValue(Type type, double time) {
		Series<Number, Number> series = allocateSeries(type);
		xPosition++;
		if (xPosition > maxElements) {
			removeFirstValueAll();
		}
		series.getData().add((new Data<Number, Number>(xPosition, time)));
		lastModifiedSeries = new PingSeries(series, type);
	}

	/**
	 * Remove first value of first series. Remove series also if it remains
	 * empty. <b>Note:</b> This removes all values, which are on the same
	 * position as the first one.
	 * 
	 * @return the removed value
	 */
	private Data<Number, Number> removeFirstValueAll() {
		Data<Number, Number> removedValue = removeFirstValue();

		Data<Number, Number> newFirstElement = getFirstValue();

		while (removedValue.getXValue() == newFirstElement.getXValue()) {
			removeFirstValue();
			newFirstElement = getFirstValue();
		}

		NumberAxis xAxis = (NumberAxis) this.getXAxis();
		xAxis.setLowerBound((newFirstElement.getXValue()).doubleValue());
		xAxis.setUpperBound(xPosition);

		return removedValue;
	}

	/**
	 * Remove the first value of first series. Remove series also if it remains
	 * empty
	 * 
	 * @return the removed value
	 */
	private Data<Number, Number> removeFirstValue() {
		Series<Number, Number> series = this.getData().get(0); // get first
		// series

		Data<Number, Number> removedValue = series.getData().remove(0);

		if (series.getData().size() == 0) { // remove empty series
			this.getData().remove(0);
		}

		return removedValue;
	}

	/** Returns first value of first series */
	private Data<Number, Number> getFirstValue() {
		return this.getData().get(0).getData().get(0);
	}

	/** Returns last value of last series */
	private Data<Number, Number> getLastValue() {
		int length = lastModifiedSeries.getSeries().getData().size();
		return lastModifiedSeries.getSeries().getData().get(length - 1);
	}
}
