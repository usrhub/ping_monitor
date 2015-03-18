package de.steinerix.ping_monitor.ping;

import java.util.EventObject;

import org.icmp4j.IcmpPingResponse;

public class DeviceEvent extends EventObject {

	/**
	 * DeviceEvent contains a {@link IcmpPingResponse} response
	 */
	private static final long serialVersionUID = 4120162064598397445L;
	private final IcmpPingResponse response;

	public DeviceEvent(Object source, IcmpPingResponse response) {
		super(source);
		this.response = response;
	}

	public IcmpPingResponse getResponse() {
		return response;
	}
}
