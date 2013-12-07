package bloodandmithril.server;

import bloodandmithril.persistence.GameLoader;
import bloodandmithril.world.GameWorld;

/**
 * Entry point class for the local game server
 *
 * @author Matt
 */
public class BloodAndMithrilServerLocal {

	public static void main(String[] args) {
		GameWorld gameWorld = new GameWorld();
		GameLoader.load();
	}
}