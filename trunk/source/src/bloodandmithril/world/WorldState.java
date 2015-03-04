package bloodandmithril.world;

import bloodandmithril.core.Copyright;

/**
 * Represents the world state - Weather, Time, Events etc.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class WorldState {

	/** THE current epoch */
	private static Epoch currentEpoch = new Epoch(16.5f, 13, 9, 2013);


	public static Epoch getCurrentEpoch() {
		return currentEpoch;
	}


	public static void setCurrentEpoch(Epoch currentEpoch) {
		WorldState.currentEpoch = currentEpoch;
	}
}