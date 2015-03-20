package de.steinerix.ping_monitor.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * DeviceConfig holds information about a network device and how it should be
 * monitored
 * 
 * @author usr
 *
 */

public class DeviceConfig {

	final private Logger log;;
	private InetAddress addr;
	private String name;
	private int interval;
	private int timeout;
	private int limit;
	private int maxGraph;
	private InternetAddress email;

	public DeviceConfig(DeviceConfig config) {
		log = Logger.getLogger(DeviceConfig.class.getName());
		try {
			this.addr = InetAddress
					.getByAddress((config.getAddr().getAddress()));
		} catch (UnknownHostException e) {
			// IP retrieved by InetAddress object
			// it is safe to assume that it is of valid length
			log.log(Level.WARNING,
					"["
							+ name
							+ "]: "
							+ "IP address invalid. This should not happen, please review client code.",
					e);
		}
		this.name = config.getName();
		this.interval = config.getInterval();
		this.timeout = config.getTimeout();
		this.limit = config.getLimit();
		this.maxGraph = config.getMaxGraph();
		try {
			this.email = new InternetAddress(config.getEmail().getAddress());
		} catch (AddressException e) {
			// E-Mail retrieved by InternetAddress object
			// it is safe to assume that it was parsed without additional syntax
			// checks
			log.log(Level.WARNING,
					"["
							+ name
							+ "]: "
							+ "Could not parse email. This should not happen, please review client code.",
					e);
		}
	}

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
	public DeviceConfig(InetAddress addr, String name, int interval,
			int timeout, int limit, int maxGraph, InternetAddress eMail) {
		log = Logger.getLogger(DeviceConfig.class.getName());

		// check arguments
		if (addr == null || name == null || eMail == null) {
			if (name != null) {
				throwArgumentException("argument may not be null", name);
			} else {
				throwArgumentException("argument may not be null", "");
			}
		}

		if (interval < 100) {
			throwArgumentException(
					"flooding protection: interval should be at least 100ms",
					name);
		}

		if (limit < 1) {
			throwArgumentException("limit should be > 0", name);
		} else if (maxGraph < limit) {
			throwArgumentException("maxGraph should be >= limit", name);
		} else if (timeout < limit) {
			throwArgumentException("timeout should be >= limit", name);
		}

		// assign values
		this.addr = addr;
		this.name = name;
		this.interval = interval;
		this.timeout = timeout;
		this.limit = limit;
		this.maxGraph = maxGraph;
		this.email = eMail;
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
	public int getInterval() {
		return interval;
	}

	/** Time in ms defining the ping timeout. */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @return Time in ms defining the trigger limit for ping response time
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
	public InternetAddress getEmail() {
		return email;
	}

	@Override
	public int hashCode() { // generated with eclipse
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addr == null) ? 0 : addr.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + (int) (interval ^ (interval >>> 32));
		result = prime * result + limit;
		result = prime * result + maxGraph;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + timeout;
		return result;
	}

	@Override
	public boolean equals(Object obj) { // generated with eclipse
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeviceConfig other = (DeviceConfig) obj;
		if (addr == null) {
			if (other.addr != null)
				return false;
		} else if (!addr.equals(other.addr))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (interval != other.interval)
			return false;
		if (limit != other.limit)
			return false;
		if (maxGraph != other.maxGraph)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (timeout != other.timeout)
			return false;
		return true;
	}

	/**
	 * throws an IllegalArgumentException with provided message and device name
	 */
	private void throwArgumentException(String message, String deviceName) {
		throw new IllegalArgumentException("[" + deviceName + "]: " + message);
	}
}
