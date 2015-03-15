package de.steinerix.ping_monitor.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides a method to read in device settings from a XML configuration file
 * 
 * @author usr
 */

public class ConfigReader {
	final private String DEVICE_XPATH = "//device";
	private Logger log = Logger.getLogger(ConfigReader.class.getName());
	private XPath xpath = XPathFactory.newInstance().newXPath();
	private File configFile;
	private InputSource source;

	/**
	 * @param configFile
	 *            XML Configuration file
	 * @throws FileNotFoundException
	 */
	public ConfigReader(File configFile) throws FileNotFoundException {
		if (!configFile.exists()) {
			throw new FileNotFoundException("Configuration file missing. "
					+ configFile.getAbsolutePath());
		}

		this.configFile = configFile;
		source = new InputSource(configFile.getAbsolutePath());

		validateConfig();

	}

	/**
	 * Reads the xml config and returns a list of device specific
	 * configurations.
	 */
	public List<DeviceConfig> read() throws XPathExpressionException {
		log.log(Level.INFO,
				"Reading config file: " + configFile.getAbsolutePath());

		ArrayList<DeviceConfig> devices = new ArrayList<DeviceConfig>();

		for (int i = 1; i < countElements(DEVICE_XPATH) + 1; i++) {
			DeviceConfig device = getDeviceConfig(i);
			devices.add(device);
		}
		return devices;
	}

	/**
	 * Validates config file according to schema
	 */
	private void validateConfig() {
		log.log(Level.INFO,
				"Validating config file: " + configFile.getAbsolutePath());

		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try {
			schema = factory.newSchema(new File(configFile.getParent()
					+ File.separator + getSchemaLocation()).toURI().toURL());
			Validator validator = schema.newValidator();

			validator.validate(new StreamSource(configFile.getAbsolutePath()));
		} catch (XPathExpressionException e) {
			String msg = "Could not get schema from: "
					+ configFile.getAbsolutePath();
			log.log(Level.WARNING, msg, e);

		} catch (SAXException | IOException e) {
			String msg = "Error validating: " + configFile.getAbsolutePath();
			log.log(Level.WARNING, msg, e);
		}
	}

	/**
	 * Get the device configuration for "&lt;device&gt;" element at specified
	 * index (1 … length)
	 * 
	 * @throws IllegalStateException
	 */
	private DeviceConfig getDeviceConfig(int index) {
		DeviceConfig device = null;

		// retrieve device values from xml file
		String ip;
		try {
			ip = getDeviceProperty(index, "ip");
			String name = getDeviceProperty(index, "name");
			int interval = Integer
					.parseInt(getDeviceProperty(index, "interval"));
			int limit = Integer.parseInt(getDeviceProperty(index, "limit"));
			int maxGraph = Integer
					.parseInt(getDeviceProperty(index, "maxgraph"));
			String eMail = getDeviceProperty(index, "email");

			// construct new device config
			device = new DeviceConfig(InetAddress.getByName(ip), name,
					interval, limit, maxGraph, new InternetAddress(eMail));

		} catch (XPathExpressionException e) {
			throwIllegalStateExceptionAndLog("Could not read device property",
					e);
		} catch (AddressException e) {
			throwIllegalStateExceptionAndLog(
					"Email in device configuration invalid", e);
		} catch (UnknownHostException e) {
			throwIllegalStateExceptionAndLog(
					"Could not retrieve IP of device (Unknown host)", e);
		}

		return device;

	}

	/**
	 * Logs the message (severe) and throws an IllegalStateException with
	 * provided message and cause.
	 */
	private void throwIllegalStateExceptionAndLog(String message,
			Throwable cause) {
		log.log(Level.SEVERE, message, cause);
		throw new IllegalStateException(message, cause);
	};

	/**
	 * Returns a device property for device specified by index (1 … length) and
	 * tag name (e. g. "email" for &lt;email&gt;)
	 * 
	 * @return string representation of property
	 * @throws XPathExpressionException
	 */
	private String getDeviceProperty(int index, String propertyName)
			throws XPathExpressionException {
		String expression = DEVICE_XPATH + "[" + index + "]/" + propertyName;
		String property = (String) xpath.evaluate(expression, source,
				XPathConstants.STRING);
		log.log(Level.FINE, "Device: " + index + ", " + propertyName + ": "
				+ property);

		return property;
	}

	/**
	 * Return count of XPath matches for provided expression
	 * 
	 * @throws XPathExpressionException
	 */
	private int countElements(String expression)
			throws XPathExpressionException {
		return ((Double) xpath.evaluate("count(" + expression + ")", source,
				XPathConstants.NUMBER)).intValue();
	}

	/**
	 * Returns scheme location
	 * 
	 * @throws XPathExpressionException
	 */
	private String getSchemaLocation() throws XPathExpressionException {
		String schemaLocation = (String) xpath.evaluate("//config/@*", source,
				XPathConstants.STRING);
		log.log(Level.INFO, "Schema for config file: " + schemaLocation);
		return schemaLocation;
	}
}