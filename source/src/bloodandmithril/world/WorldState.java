package bloodandmithril.world;

/**
 * Represents the world state - Weather, Time, Events etc.
 *
 * @author Matt
 */
public class WorldState {

	/** THE current epoch */
	private static Epoch currentEpoch = new Epoch(14.5f, 13, 9, 2013);


	public static Epoch getCurrentEpoch() {
		return currentEpoch;
	}


	public static void setCurrentEpoch(Epoch currentEpoch) {
		WorldState.currentEpoch = currentEpoch;
	}
}