package de.steinerix.ping_monitor;

import de.steinerix.ping_monitor.log.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import de.steinerix.ping_monitor.config.ConfigReader;

public class PingMonitor {

	public static void main(String[] args) {
		Log.init(Level.INFO);
		Logger log = Logger.getLogger(PingMonitor.class.getName());

		final String CONFIG_FILE = "config.xml";
		ConfigReader config = null;

		try { // define config
			config = new ConfigReader(new File(PingMonitor.class.getResource(
					"/" + CONFIG_FILE).getFile()));
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Config file not found: " + CONFIG_FILE, e);
		}

		try { // read config
			config.read();
		} catch (XPathExpressionException e) {
			log.log(Level.SEVERE, "Error parsing config: " + CONFIG_FILE, e);
		}

	}
}
