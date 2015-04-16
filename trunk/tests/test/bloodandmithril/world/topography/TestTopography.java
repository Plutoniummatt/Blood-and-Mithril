package bloodandmithril.world.topography;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import bloodandmithril.core.Copyright;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@SuppressWarnings("deprecation")
public class TestTopography {

	/**
	 * Tests {@link Topography#convertToChunkCoord(int)}
	 */
	@Test
	public void testConvertToChunkCoord() {
		assertEquals(0, Topography.convertToChunkCoord(0));

		assertEquals(-1, Topography.convertToChunkCoord(-1));
		assertEquals(-1, Topography.convertToChunkCoord(-20));
		assertEquals(-2, Topography.convertToChunkCoord(-21));

		assertEquals(0, Topography.convertToChunkCoord(1));
		assertEquals(0, Topography.convertToChunkCoord(19));
		assertEquals(1, Topography.convertToChunkCoord(20));
	}
}