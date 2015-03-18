package de.steinerix.ping_monitor.ping;

public interface DeviceListener {
	public void alarm(DeviceEvent event);

	public void clear(DeviceEvent event);

	public void reply(DeviceEvent event);
}
