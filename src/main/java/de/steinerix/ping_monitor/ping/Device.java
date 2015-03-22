package de.steinerix.ping_monitor.ping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.icmp4j.AsyncCallback;
import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;

import de.steinerix.ping_monitor.config.DeviceConfig;

/**
 * A Device represents a remote host which can be pinged.
 * 
 * @author usr
 *
 */
public class Device implements AsyncCallback<IcmpPingResponse> {
	private final Logger log = Logger.getLogger(Device.class.getName());;
	private List<DeviceListener> listeners = new ArrayList<DeviceListener>();

	private final DeviceConfig config;
	private final IcmpPingRequest pingRequest;

	private final int NUMBER_OF_RETRIES = 3;
	private final Counter limitExceeded = new Counter(NUMBER_OF_RETRIES);
	private final Counter routingErrors = new Counter(NUMBER_OF_RETRIES);
	private final Counter succesfulPings = new Counter(NUMBER_OF_RETRIES);

	private long lastPing;
	private boolean alarmFlag = false;
	private boolean pendingFlag = false;

	public Device(DeviceConfig config) {
		this.config = new DeviceConfig(config);
		pingRequest = IcmpPingUtil.createIcmpPingRequest();
		pingRequest.setHost(config.getAddr().getHostAddress());
		pingRequest.setTimeout(config.getTimeout());
	}

	/** Returns a copy of the device configuration */
	public DeviceConfig getConfig() {
		return new DeviceConfig(config);
	}

	/**
	 * Returns state of Device object. When a ping is executed, object remains
	 * with ping status "pending" until a response (ping successful, timeout,
	 * host not reachable) is captured
	 */
	public boolean isPending() {
		if (getLastPing() + (2 * config.getTimeout()) < System
				.currentTimeMillis())
			return false;
		return pendingFlag;
	}

	/** Sets ping state of device to pending */
	private void setPending() {
		pendingFlag = true;
	}

	/** Resets ping state of device to <b>false</b> (= no ping pending) */
	private void resetPending() {
		pendingFlag = false;
	}

	/** Returns alarm state of device */
	public boolean isAlarm() {
		return alarmFlag;
	}

	/** Sets device to alarm state */
	private void setAlarm(IcmpPingResponse response) {
		alarmFlag = true;
		fireAlarm(new DeviceEvent(this, response));
		log(Level.WARNING, "Alarm state entered: ");
	}

	/** Clears alarm state of device */
	private void clearAlarm(IcmpPingResponse response) {
		alarmFlag = false;
		fireClear(new DeviceEvent(this, response));
		log(Level.WARNING, "Alarm state cleared: ");
	}

	/** Updates the last ping time */
	private void updateLastPing() {
		lastPing = System.currentTimeMillis();
	}

	/** Get the time when last ping was sent */
	public long getLastPing() {
		return lastPing;
	}

	/**
	 * Execute a ping command on device (Ping will only be executed if not
	 * blocked by an already pending ping)
	 */
	public void ping() {
		if (isPending()) {
			return;
		}
		setPending();
		updateLastPing();

		IcmpPingUtil.executePingRequest(pingRequest, this); // call "this" back

	}

	/** Callback implementation for a successful IcmpPingResponse. */
	@Override
	public void onSuccess(IcmpPingResponse response) {
		fireReply(new DeviceEvent(this, response));
		log.log(Level.FINE, IcmpPingUtil.formatResponse(response));
		resetPending();

		if (response.getSuccessFlag() && !response.getTimeoutFlag()
				&& response.getRtt() <= config.getLimit()) {
			if (updateCounter(succesfulPings, limitExceeded, routingErrors)
					&& isAlarm()) {
				clearAlarm(response);
			}
		} else if (response.getSuccessFlag() && !response.getTimeoutFlag()
				&& response.getRtt() > config.getLimit()) {
			if (updateCounter(limitExceeded, routingErrors, succesfulPings)
					&& !isAlarm()) {
				setAlarm(response);
			}
		} else {
			if (updateCounter(routingErrors, succesfulPings, limitExceeded)
					&& !isAlarm()) {
				setAlarm(response);
			}
		}
	}

	/** Callback implementation for a failure IcmpPingResponse. */
	@Override
	public void onFailure(Throwable throwable) {
		log.log(Level.WARNING, "ICMP4J couldn't handle response", throwable);
		// construct a dummy response
		IcmpPingResponse response = new IcmpPingResponse();
		response.setRtt(0);
		response.setHost(this.getConfig().getAddr().getHostAddress());
		response.setTtl(0);
		response.setErrorMessage(throwable.getMessage());
		response.setSuccessFlag(false);
		response.setTimeoutFlag(false);
		fireReply(new DeviceEvent(this, response));
		resetPending();
		if (updateCounter(routingErrors, succesfulPings, limitExceeded)
				&& !isAlarm()) {
			setAlarm(response);
		}
	}

	/**
	 * Increments first counter by one and resets the other two if limit of
	 * former is reached. Otherwise they are only reset if their count didn't
	 * reach limit yet. Returns limit reached of increment counter
	 */
	private boolean updateCounter(Counter increment, Counter reset1,
			Counter reset2) {
		boolean limitReached = increment.increment();
		if (limitReached) {
			reset1.reset();
			reset2.reset();
		} else {
			if (!reset1.isLimitReached()) {
				reset1.reset();
			}
			if (!reset2.isLimitReached()) {
				reset2.reset();
			}
		}
		return limitReached;
	}

	/** Log a message with provided level (device name and IP will be attached) */
	private void log(Level level, String message) {
		log.log(level, message + config.getName() + " ("
				+ config.getAddr().getHostAddress() + ")");
	}

	/** Add listener */
	public synchronized void addListener(DeviceListener listener) {
		listeners.add(listener);
	}

	/** Remove listener */
	public synchronized void removeListener(DeviceListener listener) {
		listeners.remove(listener);
	}

	/** Reply */
	private synchronized void fireReply(DeviceEvent event) {
		for (Iterator<DeviceListener> iterator = listeners.iterator(); iterator
				.hasNext();) {
			iterator.next().reply(event);
		}
	}

	/** Alarm */
	private synchronized void fireAlarm(DeviceEvent event) {
		for (Iterator<DeviceListener> iterator = listeners.iterator(); iterator
				.hasNext();) {
			iterator.next().alarm(event);
		}
	}

	/** Clear alarm */
	private synchronized void fireClear(DeviceEvent event) {
		for (Iterator<DeviceListener> iterator = listeners.iterator(); iterator
				.hasNext();) {
			iterator.next().clear(event);
		}
	}

}
