package de.steinerix.ping_monitor;

import de.steinerix.ping_monitor.PingResponse.Type;
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

import javafx.application.Platform;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.xml.xpath.XPathExpressionException;

import org.icmp4j.IcmpPingResponse;
import org.xml.sax.SAXException;

import de.steinerix.ping_monitor.config.ConfigReader;
import de.steinerix.ping_monitor.config.DeviceConfig;
import de.steinerix.ping_monitor.config.MailConfig;

/**
 * 
 * This class integrates all modules into an application. It can be considered
 * the controller from a MVC point of view (however this doesn't apply strictly,
 * due to JavaFX requirements)
 * 
 * @author usr
 *
 */

public class PingMonitor {
	private Logger log = Logger.getLogger(PingMonitor.class.getName());
	private List<DeviceConfig> deviceConfigs;
	private MailConfig mailConfig;
	private PingDriver pingDriver;
	private PlotInterface plotOutput;

	/**
	 * Provide an instance of PlotInterface implementation which will be used by
	 * Ping Monitor to interact with the GUI
	 * 
	 * @param plotOutput
	 * */
	public PingMonitor(PlotInterface plotOutput) {
		try {
			this.plotOutput = plotOutput;

			Log.init(Level.INFO);
			log.log(Level.INFO, "Start Ping Monitor");

			final String configFileName = "config.xml";
			readConfig(configFileName);

			pingDriver = new PingDriver(); // start ping driver

			addDevices();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Uncaught exception ", e);
			shutdown(1);
		}
	}

	/** Shutdown application with a System.exit call */
	public void shutdown(int exitCode) {
		if (exitCode == 0) {
			log.log(Level.INFO, "Exit application with code " + exitCode);
		} else {
			log.log(Level.SEVERE, "Abort application with code " + exitCode);
		}
		Platform.exit();
		System.exit(exitCode);
	}

	/**
	 * Adds all devices to the ping driver and the plot interface implementation
	 */
	private void addDevices() {
		for (Iterator<DeviceConfig> iterator = deviceConfigs.iterator(); iterator
				.hasNext();) {
			DeviceConfig deviceConfig = iterator.next();

			Device device = new Device(deviceConfig);
			int pingGraphId = plotOutput.addPingGraph(deviceConfig.getName(),
					deviceConfig.getAddr(), deviceConfig.getMaxGraph(),
					deviceConfig.getInterval(), deviceConfig.getLimit());

			addListenerToDevice(device, pingGraphId);
		}

	}

	/** add listener to provided device */
	private void addListenerToDevice(Device device, int guiDeviceId) {

		DeviceConfig deviceConfig = device.getConfig();
		device.addListener(new DeviceListener() {
			@Override
			public void alarm(DeviceEvent event) {
				Type type = getType(event.getResponse(),
						deviceConfig.getLimit());

				String subject = "alarm: " + deviceConfig.getName() + " "
						+ deviceConfig.getAddr().getHostAddress();

				String message = subject
						+ " is not operating in expected parameters\nreason: "
						+ type.toString() + "\nplease invesigate further";

				sendNotification(mailConfig, deviceConfig.getEmail(), subject,
						message);
			}

			@Override
			public void clear(DeviceEvent event) {
				String subject = "clear: " + deviceConfig.getName() + " "
						+ deviceConfig.getAddr().getHostAddress();

				String message = subject
						+ " operational within expected parameters"
						+ "\nprevious alarm state cleared";

				sendNotification(mailConfig, deviceConfig.getEmail(), subject,
						message);
			}

			@Override
			public void reply(DeviceEvent event) {
				double time = event.getResponse().getRtt();
				int limit = deviceConfig.getLimit();
				Type type = getType(event.getResponse(), limit);
				PingResponse response = new PingResponse(type, time);
				plotOutput.updatePingGraph(guiDeviceId, response);
			}
		});
		pingDriver.registerDevice(device);
	}

	/** read config */
	private void readConfig(String configFileName) {
		deviceConfigs = new ArrayList<DeviceConfig>();
		ConfigReader config = null;

		try {
			config = new ConfigReader(new File(PingMonitor.class.getResource(
					"/" + configFileName).getFile()));
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Config file not found: " + configFileName, e);
			shutdown(1);
		} catch (SAXException e) {
			log.log(Level.SEVERE, "Error reading configuration: "
					+ configFileName, e);
			shutdown(1);
		}
		try {
			deviceConfigs = config.getDeviceConfigs();
		} catch (XPathExpressionException e) {
			log.log(Level.SEVERE, "Error reading configuration: "
					+ configFileName, e);
			shutdown(1);
		}

		mailConfig = config.getMailConfig();
	}

	/** send a mail */
	private void sendNotification(MailConfig mailConfig, InternetAddress to,
			String subject, String body) {
		try {
			if (mailConfig.isEnabled()) {
				Mail.sendMessage(mailConfig, to, subject, body);
			}
		} catch (MessagingException e) {
			log.log(Level.SEVERE,
					"Couldn't send notification - Please check your connection and/or email settings",
					e);
		}
	}

	/** returns the response type */
	private Type getType(IcmpPingResponse response, int limit) {
		Type type;
		if (response.getSuccessFlag() && !response.getTimeoutFlag()
				&& response.getRtt() <= limit) {
			type = Type.NORMAL;
		} else if (response.getSuccessFlag() && !response.getTimeoutFlag()
				&& response.getRtt() > limit) {
			type = Type.LIMIT_EXCEEDED;
		} else if (response.getTimeoutFlag()) {
			type = Type.TIMEOUT;
		} else {
			type = Type.NOT_REACHABLE;
		}
		return type;
	}
}
