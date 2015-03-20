package de.steinerix.ping_monitor.ping;

/**
 * Interface for a DeviceListener
 * 
 * @author usr
 *
 */
public interface DeviceListener {
	public void alarm(DeviceEvent event);

	public void clear(DeviceEvent event);

	public void reply(DeviceEvent event);
}
