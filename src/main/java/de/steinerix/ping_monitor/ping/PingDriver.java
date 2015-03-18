package de.steinerix.ping_monitor.ping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.steinerix.ping_monitor.config.DeviceConfig;

public class PingDriver implements Runnable {
	private Logger log = Logger.getLogger(PingDriver.class.getName());

	List<Device> devices = new ArrayList<Device>();

	public PingDriver() {
		Thread t = new Thread(this);
		t.start();
	}

	public void registerDevice(Device device) {
		if (!devices.contains(device)) {
			synchronized (this) {
				devices.add(device);
				log.log(Level.INFO, "Device registered in PingDriver ("
						+ device.getConfig().getName() + ")");
			}
		}
	}

	synchronized public boolean deregisterDevice(Device device) {
		if (devices.remove(device)) {
			log.log(Level.INFO, "Device deregistered from PingDriver ("
					+ device.getConfig().getName() + ")");
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(10); // let the CPU rest for a while
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			synchronized (this) {
				for (Iterator<Device> iterator = devices.iterator(); iterator
						.hasNext();) {
					Device device = iterator.next();
					try {
						Thread.sleep(10); // separate the system calls
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
