package de.steinerix.ping_monitor.config;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;

public class ConfigReaderTest {

	@Test
	public void shouldReadValidConfig() throws FileNotFoundException,
			XPathExpressionException {
		ConfigReader config = new ConfigReader(
				openFile("test-config-valid.xml"));
		config.read();
	}

	@Test
	public void shouldContainTestData() throws FileNotFoundException,
			XPathExpressionException {
		List<DeviceConfig> devices = new ArrayList<DeviceConfig>();

		ConfigReader config = new ConfigReader(
				openFile("test-config-valid.xml"));
		devices = config.read();

		// check complete data set of first entry
		assertTrue("Test data contains three device entries.",
				devices.size() == 3);
		assertTrue(devices.get(0).getAddr() + " does not match 127.0.0.1",
				devices.get(0).getAddr().getHostAddress().equals("127.0.0.1"));
		assertTrue(devices.get(0).geteMail() + " does not match test@test.com",
				devices.get(0).geteMail().getAddress().equals("test@test.com"));
		assertTrue(devices.get(0).getInterval() + " did not match 355", devices
				.get(0).getInterval() == 355);
		assertTrue(devices.get(0).getLimit() + " did not match 37", devices
				.get(0).getLimit() == 37);
		assertTrue(devices.get(0).getMaxGraph() + " did not match 500", devices
				.get(0).getMaxGraph() == 500);
		assertTrue(devices.get(0).getName() + " did not match Test machine",
				devices.get(0).getName().equals("Test machine"));

		// check second entry
		assertTrue(devices.get(1).getName() + " did not match Test machine 2",
				devices.get(2).getName().equals("Test machine 3"));

		// check third entry
		assertTrue(devices.get(2).getName() + " did not match Test machine 3",
				devices.get(2).getName().equals("Test machine 3"));
	}

	@Test
	public void shouldNotReadInvalidConfig() throws FileNotFoundException,
			XPathExpressionException {
		try {
			ConfigReader config = new ConfigReader(
					openFile("test-config-invalid.xml"));
			config.read();
			fail();
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void shouldNotReadMalformedConfig() throws FileNotFoundException {
		try {
			System.err
					.println("Expected output of [Fatal Error] on error stream due to test -");
			ConfigReader config = new ConfigReader(
					openFile("test-config-malformed.xml"));
			config.read();
			fail();
		} catch (XPathExpressionException e) {

		}
		// catch (RuntimeException e) {
		// assertTrue(e.getMessage().equals("Malformed XML config."));
		// }

	}

	@Test
	public void shouldThrowExceptionForNonExistingConfig() {
		try {
			new ConfigReader(openFile("test-config-notexisting.xml"));
			fail();
		} catch (FileNotFoundException e) {
		}
	}

	private File openFile(String fileName) {
		try {
			return new File(this.getClass().getResource("/" + fileName)
					.getFile());
		} catch (NullPointerException e) {
			return new File(fileName);
		}
	}
}
