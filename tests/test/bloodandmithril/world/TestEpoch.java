package bloodandmithril.world;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import bloodandmithril.core.Copyright;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@SuppressWarnings("deprecation")
public class TestEpoch {

	/**
	 * Tests incrementing time
	 */
	@Test
	public void testIncrementTime() {
		Epoch testEpoch = new Epoch(0, 1, 2, 3123);

		// Increment by 10 real seconds
		testEpoch.incrementTime(10f);
		assertEquals(10f / 60f / 90f * 24f, testEpoch.getTime(), 0.001f);
		assertEquals(1, testEpoch.dayOfMonth);
		assertEquals(2, testEpoch.monthOfYear);
		assertEquals(3123, testEpoch.year);

		Epoch testEpoch2 = new Epoch(0, 1, 2, 3123);
		// Increment by 5410 seconds (1 game day + 10 real seconds)
		testEpoch2.incrementTime(5410f);
		assertEquals(10f / 60f / 90f * 24f, testEpoch2.getTime(), 0.001f);
		assertEquals(2, testEpoch2.dayOfMonth);
		assertEquals(2, testEpoch2.monthOfYear);
		assertEquals(3123, testEpoch2.year);
	}
}