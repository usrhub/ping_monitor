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

import de.steinerix.ping_monitor.config.MailConfig.AuthType;
import de.steinerix.ping_monitor.config.MailConfig.SecurityType;

/**
 * Provides methods to read in device and mail settings from provided XML
 * configuration file
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
	 *             if config file could not be found
	 * @throws SAXException
	 *             if config validation failed
	 */
	public ConfigReader(File configFile) throws FileNotFoundException,
			SAXException {
		if (!configFile.exists()) {
			// we may be executed from within a jar
			this.configFile = new File(getJarDirectory() + File.separator
					+ configFile.getName());
			if (!this.configFile.exists()) {
				throw new FileNotFoundException("Could not find config file: "
						+ configFile.getName());
			}
		} else { // file is in ordinary working directory present
			this.configFile = configFile;
		}
		source = new InputSource(this.configFile.getAbsolutePath());
		validateConfig();

	}

	/**
	 * if executed within jar directory this method returns the path where the
	 * jar is located
	 */
	private File getJarDirectory() {
		return new File(getClass().getProtectionDomain().getCodeSource()
				.getLocation().getPath()).getParentFile();
	}

	/**
	 * Reads the xml config and returns a list of device specific
	 * configurations.
	 */
	public List<DeviceConfig> getDeviceConfigs()
			throws XPathExpressionException {
		log.log(Level.INFO, "Reading device configurations from file: "
				+ configFile.getAbsolutePath());

		ArrayList<DeviceConfig> devices = new ArrayList<DeviceConfig>();

		for (int i = 1; i < countElements(DEVICE_XPATH) + 1; i++) {
			DeviceConfig device = getDeviceConfig(i);
			if (devices.contains(device)) {
				throw new IllegalStateException(
						"Device configs should be unique. Please correct XML config. ("
								+ device.getName() + " "
								+ device.getAddr().getHostAddress() + ")");
			} else {
				devices.add(device);
			}

		}
		return devices;
	}

	/**
	 * Reads the xml config and returns the mail config
	 * 
	 * @throws UnknownHostException
	 * @throws AddressException
	 */
	public MailConfig getMailConfig() {
		log.log(Level.INFO,
				"Reading mail config from file: "
						+ configFile.getAbsolutePath());

		MailConfig config = null;

		try {
			boolean enabled = (getMailProperty("enabled")).toLowerCase()
					.equals("true");
			AuthType authType = AuthType.valueOf(getMailProperty("authtype"));
			SecurityType securityType = SecurityType
					.valueOf(getMailProperty("securitytype"));
			InetAddress server = null;
			try {
				server = InetAddress.getByName(getMailProperty("server"));
			} catch (UnknownHostException e) {
				// warn in logs and deactivate mail- this behaviour is
				// intentional

				if (enabled) {
					log.log(Level.WARNING,
							"mail disabled - please check your smtp settings (unknown host)",
							e);
				} else { // if mail is disabled user probably doesn't care
							// anyways
					log.log(Level.WARNING, "smtp settings (unknown host)");
				}

				enabled = false;
			}
			InternetAddress from = new InternetAddress(getMailProperty("from"));
			String username = getMailProperty("username");
			String password = getMailProperty("password");
			int port;

			port = Integer.parseInt(getMailProperty("port"));

			config = new MailConfig.Builder().server(server, port, enabled)
					.type(authType, securityType)
					.credentials(from, username, password).build();
		} catch (XPathExpressionException e) {
			throwIllegalStateExceptionAndLog("Could not read mail property", e);
		} catch (AddressException e) {
			throwIllegalStateExceptionAndLog(
					"Email in mail configuration invalid", e);
		}
		return config;
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

			String tmpTimeout = getOptionalDeviceProperty(index, "timeout");
			int timeout = tmpTimeout.equals("") ? interval : Integer
					.parseInt(tmpTimeout);

			int maxGraph = Integer
					.parseInt(getDeviceProperty(index, "maxgraph"));
			String eMail = getDeviceProperty(index, "email");

			// construct new device config
			device = new DeviceConfig(InetAddress.getByName(ip), name,
					interval, timeout, limit, maxGraph, new InternetAddress(
							eMail));

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
	 * Validates config file according to schema
	 * 
	 * @throws SAXException
	 */
	private void validateConfig() throws SAXException {
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
		} catch (XPathExpressionException | IOException e) {
			String msg = "Could not get schema from: "
					+ configFile.getAbsolutePath();
			log.log(Level.WARNING, msg, e);

		} catch (SAXException e) {
			// log and rethrow intended
			String msg = "Error validating: " + configFile.getAbsolutePath();
			log.log(Level.WARNING, msg, e);
			throw e;
		}
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
	private String getProperty(int index, String parentElement,
			String propertyName) throws XPathExpressionException {
		String expression = "//" + parentElement + "[" + index + "]/"
				+ propertyName;
		String property = (String) xpath.evaluate(expression, source,
				XPathConstants.STRING);
		log.log(Level.FINE, parentElement + ": " + index + ", " + propertyName
				+ ": " + property);

		return property;
	}

	/**
	 * Returns a mail property
	 * 
	 * @return string representation of property
	 * @throws XPathExpressionException
	 */
	private String getMailProperty(String propertyName)
			throws XPathExpressionException {
		return getProperty(1, "mail", propertyName); // only one mail element
	}

	/**
	 * Returns a device property for device specified by index (1 … length) and
	 * tag name (e. g. "email" for &lt;email&gt;)
	 * 
	 * @return string representation of property
	 * @throws XPathExpressionException
	 */
	private String getDeviceProperty(int index, String propertyName)
			throws XPathExpressionException {
		return getProperty(index, "device", propertyName);
	}

	/**
	 * Returns an optional device property for device specified by index (1 …
	 * length) and tag name (e. g. "email" for &lt;email&gt;). If property isn't
	 * present, an empty string is returned.
	 * 
	 * @return string representation of optional property - in case it isn't
	 *         set, the string is empty.
	 * @throws XPathExpressionException
	 */
	private String getOptionalDeviceProperty(int index, String propertyName)
			throws XPathExpressionException {
		String expression = DEVICE_XPATH + "[" + index + "]/" + propertyName;
		String property = "";
		if (countElements(expression) > 0) {
			property = (String) xpath.evaluate(expression, source,
					XPathConstants.STRING);

		}

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