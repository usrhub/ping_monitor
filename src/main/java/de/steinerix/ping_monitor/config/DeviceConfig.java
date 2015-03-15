package de.steinerix.ping_monitor.config;

import java.net.InetAddress;

import javax.mail.internet.InternetAddress;

/**
 * DeviceConfig holds information about a network device and how it should be
 * monitored
 * 
 * @author usr
 *
 */

public class DeviceConfig {

	private InetAddress addr;
	private String name;
	private long interval;
	private int limit;
	private int maxGraph;
	private InternetAddress eMail;

	/**
	 * 
	 * @param addr
	 *            // * InetAddress object (Can be constructed by a raw IP or
	 *            host name)
	 * @param name
	 *            Name of the device
	 * @param interval
	 *            Time in ms defining the interval by which the device is pinged
	 * @param limit
	 *            Time in ms defining the limit for ping response time
	 * @param maxGraph
	 *            Time in ms which equals 100% of the graph's size
	 * @param eMail
	 *            InternetAddress object (Can be instantiated by a String)
	 */
	public DeviceConfig(InetAddress addr, String name, long interval,
			int limit, int maxGraph, InternetAddress eMail) {

		// check arguments
		if (addr == null || name == null || eMail == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		if (interval < 0 || limit < 0 || maxGraph < 0) {
			throw new IllegalArgumentException("Negative value.");
		} else if (maxGraph < limit) {
			throw new IllegalArgumentException("maxGraph should be >= limit ");
		}

		// assign values
		this.addr = addr;
		this.name = name;
		this.interval = interval;
		this.limit = limit;
		this.maxGraph = maxGraph;
		this.eMail = eMail;
	}

	/**
	 * @return InetAddress object (Can be constructed by a raw IP or host name)
	 */
	public InetAddress getAddr() {
		return addr;
	}

	/**
	 * @return Name of the device
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Time in ms defining the interval by which the device is pinged
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * @return Time in ms defining the limit for ping response time
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * @return Time in ms which equals 100% of the graph's size
	 */
	public int getMaxGraph() {
		return maxGraph;
	}

	/**
	 * @return InternetAddress object (Can be instantiated by a String)
	 */
	public InternetAddress geteMail() {
		return eMail;
	}

}
