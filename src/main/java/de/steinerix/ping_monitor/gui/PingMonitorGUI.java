package de.steinerix.ping_monitor.gui;

import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.steinerix.ping_monitor.PingMonitor;
import de.steinerix.ping_monitor.PingResponse;
import de.steinerix.ping_monitor.PlotInterface;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class PingMonitorGUI extends Application implements PlotInterface {
	private final Logger log = Logger.getLogger(PingMonitorGUI.class.getName());
	private HashMap<Integer, PingChart> pingCharts = new HashMap<Integer, PingChart>();
	private GridPane pingChartGrid;
	private final int MAX_PING_CHARTS = 20;
	private final int CHART_COLUMNS = 5;

	public static void main(String args[]) {
		launch(args);
	}

	@Override
	public void start(Stage arg0) {
		// build GUI
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// basic GUI layout etc.
				log.log(Level.INFO, "Initialize GUI");
				arg0.setTitle("Ping Monitor - Watch your digital neighbourhood 0.2"); // :-)
				pingChartGrid = new GridPane();
				Scene scene = new Scene(pingChartGrid);
				arg0.setMinWidth(1000);
				arg0.setMinHeight(755);

				arg0.setScene(scene);
				arg0.show();
			}
		});

		// start application
		PingMonitor monitor = new PingMonitor(this);

		// ensure application is shutdown when window gets closed
		arg0.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				monitor.shutdown(0);
			}
		});

	}

	// provide interface for ping monitor
	@Override
	synchronized public int addPingGraph(String name, InetAddress ip,
			int maxGraph, int interval, int limit) {
		int size = pingCharts.size();

		if (size < MAX_PING_CHARTS) {
			final int row = size / CHART_COLUMNS;
			final int column = size % CHART_COLUMNS;
			PingChart pingChart = addPingChart(name, ip, maxGraph, interval,
					limit, row, column);
			pingCharts.put(pingCharts.size(), pingChart);
			int id = pingCharts.size() - 1;
			log.log(Level.INFO, "Added PingChart for " + name + " [id: " + id
					+ "]");
			return id;
		}
		return size - 1;
	}

	/** Adds a new PingChart to GUI */
	private PingChart addPingChart(String name, InetAddress ip, int maxGraph,
			int interval, int limit, int row, int column) {
		// Axis
		NumberAxis xAxis = new NumberAxis();

		xAxis.setLabel("lim: " + formatMilliseconds(limit) + " intvl: "
				+ formatMilliseconds(interval));

		xAxis.setStyle("-fx-font-size:10px;"); // label font size
		xAxis.setTickLabelFont(new Font(8)); // tick label font size

		NumberAxis yAxis = new NumberAxis();

		// PingChart
		int displayedValues = 15;
		PingChart pingChart = new PingChart(xAxis, yAxis, displayedValues,
				maxGraph);
		pingChart.setAnimated(false);

		// Labels
		Label labelName = new Label();
		Label labelIp = new Label();
		labelName.setText(name);
		labelIp.setText("(" + ip.getHostAddress() + ")");
		labelIp.setFont(new Font(10));

		VBox vBox = new VBox();
		vBox.setAlignment(Pos.CENTER);

		// Add to GUI
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				vBox.getChildren().addAll(labelName, labelIp, pingChart);
				pingChartGrid.add(vBox, column, row);
			}
		});

		return pingChart;
	}

	// provide interface for ping monitor
	@Override
	public void updatePingGraph(int id, PingResponse response) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				log.log(Level.FINE, "Update PingChart: " + id);
				pingCharts.get(id).addPingResponse(response.getType(),
						response.getTime());

			}
		});
	}

	/** Returns a formatted representation of value (provide in ms). */
	private String formatMilliseconds(int value) {
		if (value >= 3600 * 1000) { // as hours
			return round(value / (3600 * 1000.0)) + "h";
		} else if (value >= 60 * 1000) { // as minutes
			return round(value / (60 * 1000.0)) + "min";
		} else if (value >= 1000) { // as seconds
			return (value / 1000) + "s";
		} else { // as milliseconds
			return value + "ms";
		}
	}

	/** format double value with 0.# */
	private String round(double value) {
		// fraction part should be optional and rounded to one digit
		return new DecimalFormat("0.#").format(value);
	}

}