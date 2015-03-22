package de.steinerix.ping_monitor.gui;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.steinerix.ping_monitor.PingMonitor;
import de.steinerix.ping_monitor.PingResponse;
import de.steinerix.ping_monitor.PlotInterface;

/** This class allows a headless run of ping monitor */
public class Headless implements PlotInterface {
	public static void main(String args[]) {
		Logger log = Logger.getLogger(Headless.class.getName());
		log.log(Level.INFO, "Initialize headless session");
		Headless headless = new Headless();
		new PingMonitor(headless); // start application
	}

	// dummy implementations
	@Override
	public int addPingGraph(String name, InetAddress ip, int maxGraph,
			int limit, int interval) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updatePingGraph(int id, PingResponse response) {
		// TODO Auto-generated method stub

	}

}
