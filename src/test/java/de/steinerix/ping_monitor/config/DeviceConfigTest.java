package de.steinerix.ping_monitor.config;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.junit.Test;

public class DeviceConfigTest {

	@Test
	public void testConstructor() throws AddressException, UnknownHostException {
		// assert object holds right values
		InternetAddress eMail = new InternetAddress("daniel@steinerix.de");
		String ip = "127.0.0.1", name = "Test machine";
		int limit = 500, maxGraph = 5000;
		long interval = 1000;

		DeviceConfig config = new DeviceConfig(InetAddress.getByName(ip), name,
				interval, limit, maxGraph, eMail);

		assertEquals(config.getAddr().getHostAddress(), ip);
		assertEquals(name, config.getName());
		assertEquals(limit, config.getLimit());
		assertEquals(interval, config.getInterval());
		assertEquals(eMail, config.geteMail());

		// assert IllegalArgumentExceptions are thrown when initialized with bad
		// arguments
		try { // name
			config = new DeviceConfig(InetAddress.getByName(ip), null,
					interval, limit, maxGraph, eMail);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try { // interval
			config = new DeviceConfig(InetAddress.getByName(ip), name, -1,
					limit, maxGraph, eMail);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try { // alarm
			config = new DeviceConfig(InetAddress.getByName(ip), name,
					interval, -1, maxGraph, eMail);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try { // maxGraph
			config = new DeviceConfig(InetAddress.getByName(ip), name,
					interval, limit, -1, eMail);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try { // eMail
			config = new DeviceConfig(InetAddress.getByName(ip), name,
					interval, limit, maxGraph, null);
			fail();
		} catch (IllegalArgumentException e) {
		}

		try { // maxGraph < limit
			config = new DeviceConfig(InetAddress.getByName(ip), name,
					interval, limit, limit - 1, eMail);
			fail();
		} catch (IllegalArgumentException e) {
		}

	}
}
