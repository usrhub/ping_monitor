package de.steinerix.ping_monitor;

import java.net.InetAddress;

/**
 * This interface should be implemented by the GUI module
 * 
 * @author usr
 *
 */

public interface PlotInterface {
	public int addPingGraph(String name, InetAddress ip, int maxGraph,
			int limit, int interval);

	public void updatePingGraph(int id, PingResponse response);
}
