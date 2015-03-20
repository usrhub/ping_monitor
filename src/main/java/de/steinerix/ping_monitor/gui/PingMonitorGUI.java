package de.steinerix.ping_monitor.gui;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.SAXException;

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
	final private Logger log = Logger.getLogger(PingMonitorGUI.class.getName());;
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
				arg0.setTitle("Ping Monitor");
				pingChartGrid = new GridPane();
				Scene scene = new Scene(pingChartGrid);
				arg0.setMinWidth(1024);
				arg0.setMinHeight(768);
				arg0.setMaxWidth(1024);
				arg0.setMaxHeight(768);

				arg0.setScene(scene);
				arg0.show();
			}
		});

		// start application
		PingMonitor monitor = new PingMonitor();
		try {
			monitor.pingMonitor(this);
		} catch (InterruptedException e) {
			exit(e);
		} catch (SAXException e) {
			exit(e);
		}

		// ensure application gets exited if window is closed
		arg0.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				exit(0);
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

		xAxis.setLabel("limit: " + limit + "ms / interval: "
				+ formatInterval(interval));

		xAxis.setStyle("-fx-font-size:10px;"); // label font size
		xAxis.setTickLabelFont(new Font(8)); // tick label font size

		NumberAxis yAxis = new NumberAxis();

		// PingChart
		PingChart pingChart = new PingChart(xAxis, yAxis, 15, maxGraph);
		pingChart.setMaxHeight(80);
		pingChart.setMaxWidth(100);
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

	/** Returns a formatted representation of interval (provide in ms). */
	private String formatInterval(int interval) {
		String formattedInterval;
		if (interval > 3600 * 1000) { // as hours
			formattedInterval = (interval / (3600 * 1000L)) + "h";
		}
		if (interval > 60 * 1000) { // as minutes
			formattedInterval = (interval / (3600 * 1000L)) + "h";
		}
		if (interval > 1000) { // as seconds
			formattedInterval = (interval / 1000L) + "s";
		} else { // as milliseconds
			formattedInterval = interval + "ms";
		}
		return formattedInterval;
	}

	/** abort application */
	private void exit(Exception e) {
		log.log(Level.SEVERE, "Aborting application", e);
		Platform.exit();
		exit(1);
	}

	/** exit application */
	private void exit(int exitCode) {
		log.log(Level.INFO, "Exit application with code " + exitCode);
		Platform.exit();
		System.exit(exitCode);
	}
}