package de.steinerix.ping_monitor.ping;

/**
 * This class defines a simple counter that can test if a limit was reached.
 * However the <b>counter counts beyond that limit</b> and will continue with
 * value 1 if Integer.MAX_VALUE is incremented.
 * 
 * @author usr
 *
 */

class Counter {
	final int LIMIT;
	int count = 0;

	/** Initialize counter with Integer.MAX_VALUE as limit */
	Counter() {
		LIMIT = Integer.MAX_VALUE;
	}

	/** Initialize counter with a positive integer as limit */
	Counter(int limit) {
		if (limit < 0) {
			throw new IllegalArgumentException(
					"Counter limit must be non negative");
		}
		LIMIT = limit;
	}

	/**
	 * Increments the counter by one and returns true if specified LIMIT is
	 * exceeded (true if >= LIMIT). <br />
	 * Note that in case of integer overflow (counter reaches Integer.MAX_VALUE)
	 * the counter is set to 1 and method will return <b>false</b>!
	 */
	public boolean increment() {
		// prevent overflow
		if (count == Integer.MAX_VALUE) {
			count = 1;
			return false;
		}

		// increment count and return limit reached state
		count++;
		if (count >= LIMIT) {
			return true;
		} else {
			return false;
		}
	}

	/** Resets counter to 0 */
	public void reset() {
		count = 0;
	}

	/** Returns true if count is >= specified LIMIT */
	public boolean isLimitReached() {
		if (count >= LIMIT) {
			return true;
		} else {
			return false;
		}
	}

	/** Returns current count */
	public int getCount() {
		return count;
	}

	/** Returns the specified LIMIT */
	public int getLimit() {
		return LIMIT;
	}
}
