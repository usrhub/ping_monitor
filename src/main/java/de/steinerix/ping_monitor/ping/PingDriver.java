package de.steinerix.ping_monitor.ping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.icmp4j.IcmpPingUtil;

import de.steinerix.ping_monitor.config.DeviceConfig;

/**
 * Pings all registered devices according to their interval time.
 * 
 * @author usr
 *
 */
public class PingDriver implements Runnable {
	private final Logger log = Logger.getLogger(PingDriver.class.getName());
	private List<Device> devices = new ArrayList<Device>();

	/** The PingDriver instance starts automatically */
	public PingDriver() {
		Thread t = new Thread(this);
		t.start();
	}

	/** Register a device in PingDriver */
	public void registerDevice(Device device) {
		if (!devices.contains(device)) {
			synchronized (this) {
				devices.add(device);
				log.log(Level.INFO, "Device registered in PingDriver ("
						+ device.getConfig().getName() + ")");
			}
		}
	}

	/** De-register a device from PingDriver. */
	synchronized public boolean deregisterDevice(Device device) {
		if (devices.remove(device)) {
			log.log(Level.INFO, "Device deregistered from PingDriver ("
					+ device.getConfig().getName() + ")");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Run loop
	 */
	@Override
	public void run() {
		// init IcmpPingUtil subsystem with a first, blocking ping.
		IcmpPingUtil.executePingRequest("127.0.0.1", 40, 300);

		while (true) {
			try {
				Thread.sleep(10); // let the CPU rest for a while
			} catch (InterruptedException e) {
				log.log(Level.INFO, "PingDriver has been interrupted.", e);
			}

			// ping devices
			synchronized (this) {
				for (Iterator<Device> iterator = devices.iterator(); iterator
						.hasNext();) {
					Device device = iterator.next();
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						log.log(Level.INFO,
								"SyncPingDriver has been interrupted.", e);
					}
					DeviceConfig config = device.getConfig();
					if (device.getLastPing() + config.getInterval() < System
							.currentTimeMillis()) {
						device.ping();
					}
				}
			}
		}
	}
}
