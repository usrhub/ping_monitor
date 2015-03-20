package de.steinerix.ping_monitor;

/**
 * Provides a simple ping response representation.
 * 
 * @author usr
 *
 */
public class PingResponse {
	public enum Type {
		NORMAL, LIMIT_EXCEEDED, TIMEOUT, NOT_REACHABLE
	}

	final private Type type;
	final private double time;

	public PingResponse(Type type, Double time) {
		this.type = type;
		this.time = time;
	}

	public Type getType() {
		return type;
	}

	public double getTime() {
		return time;
	}
}
