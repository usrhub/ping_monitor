package de.steinerix.ping_monitor.ping;

import static org.junit.Assert.*;

import org.junit.Test;

public class CounterTest {

	@Test
	public void shouldInitializeLimitsAsExpected() {
		Counter counter = new Counter(897);
		assertTrue(counter.getLimit() == 897);
		counter = new Counter();
		assertTrue(counter.getLimit() == Integer.MAX_VALUE);
		try {
			counter = new Counter(-1);
			fail();
		} catch (IllegalArgumentException e) {
		}

	}

	@Test
	public void shouldIncrement() {
		Counter counter = new Counter(897);

		assertTrue("Should not reach limit of 897",
				counter.increment() == false);
		assertTrue(counter.getCount() + " does not match 1",
				counter.getCount() == 1);
	}

	@Test
	public void shouldReset() {
		Counter counter = new Counter();

		counter.count = 95;
		assertTrue(counter.getCount() == 95); // just in case

		counter.reset();
		assertTrue(counter.getCount() + " does not match 0",
				counter.getCount() == 0);
	}

	@Test
	public void shouldHandleIntegerOverflow() {
		Counter counter = new Counter();

		// set internal count value
		counter.count = Integer.MAX_VALUE;
		assertTrue(counter.getCount() == Integer.MAX_VALUE);

		// assert integer overflow is handled correctly
		assertTrue("Should return false in case of overflow",
				!counter.increment());
		assertTrue(counter.getCount() + " does not match 1",
				counter.getCount() == 1); // contract
	}

	@Test
	public void shouldReturnReachedLimit() {
		Counter counter = new Counter(100);

		counter.count = 99;
		assertTrue(counter.getCount() == 99); // just in case
		assertTrue("Limit should not be reached", (!counter.isLimitReached()));

		counter.increment();
		assertTrue("Limit should be reached", (counter.isLimitReached()));

		counter.increment(); // value = limit + 1
		assertTrue("Limit should be reached", (counter.isLimitReached()));

		counter = new Counter();
		counter.count = Integer.MAX_VALUE;
		assertTrue("Limit should be reached", (counter.isLimitReached()));
	}

}
