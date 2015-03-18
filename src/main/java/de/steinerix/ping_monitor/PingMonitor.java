package de.steinerix.ping_monitor;

import de.steinerix.ping_monitor.log.Log;
import de.steinerix.ping_monitor.mail.Mail;
import de.steinerix.ping_monitor.ping.Device;
import de.steinerix.ping_monitor.ping.DeviceEvent;
import de.steinerix.ping_monitor.ping.DeviceListener;
import de.steinerix.ping_monitor.ping.PingDriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.xml.xpath.XPathExpressionException;

import de.steinerix.ping_monitor.config.ConfigReader;
import de.steinerix.ping_monitor.config.DeviceConfig;
import de.steinerix.ping_monitor.config.MailConfig;

public class PingMonitor {

	synchronized public static void main(String[] args)
			throws InterruptedException {
		Log.init(Level.FINE);
		Logger log = Logger.getLogger(PingMonitor.class.getName());

		final String CONFIG_FILE = "config.xml";

		List<DeviceConfig> configs = new ArrayList<DeviceConfig>();
		ConfigReader config = null;

		try { // define config
			config = new ConfigReader(new File(PingMonitor.class.getResource(
					"/" + CONFIG_FILE).getFile()));
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Config file not found: " + CONFIG_FILE, e);
		}

		try { // read config
			configs = config.getDeviceConfigs();
		} catch (XPathExpressionException e) {
			log.log(Level.SEVERE, "Error parsing config: " + CONFIG_FILE, e);
		}

		// start ping driver
		PingDriver pingDriver = new PingDriver();

		MailConfig mailConfig = config.getMailConfig();

		// add devices to ping executor
		for (Iterator<DeviceConfig> iterator = configs.iterator(); iterator
				.hasNext();) {
			DeviceConfig deviceConfig = iterator.next();
			Device device = new Device(deviceConfig);
			device.addListener(new DeviceListener() {
				@Override
				public void alarm(DeviceEvent event) {
					sendNotification(
							mailConfig,
							deviceConfig.getEmail(),
							deviceConfig.getAddr().getHostAddress() + ": clear",
							deviceConfig.getAddr().getHostAddress() + ": alarm");
				}

				@Override
				public void clear(DeviceEvent event) {
					sendNotification(
							mailConfig,
							deviceConfig.getEmail(),
							deviceConfig.getAddr().getHostAddress() + ": clear",
							deviceConfig.getAddr().getHostAddress() + ": clear");
				}

				@Override
				public void reply(DeviceEvent event) {
					System.out.println("received a reply");
				}
			});
			pingDriver.registerDevice(device);
		}
	}

	private static void sendNotification(MailConfig mailConfig,
			InternetAddress to, String subject, String body) {
		try {
			if (mailConfig.isEnabled()) {
				Mail.sendMessage(mailConfig, to, subject, body); // TODO: email
																	// from
				// device config
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
