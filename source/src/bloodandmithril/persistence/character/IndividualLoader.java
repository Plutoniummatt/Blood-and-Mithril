package bloodandmithril.persistence.character;

import static bloodandmithril.persistence.PersistenceUtil.decode;

import bloodandmithril.character.Individual;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.Gdx;

/**
 * Loads {@link Individual}s
 *
 * @author Matt
 */
public class IndividualLoader {

	/**
	 * Loads all {@link Individual}s
	 */
	public static void loadAll() {
		try {
			GameWorld.individuals = decode(Gdx.files.local(GameSaver.savePath + "/world/individuals.txt"));
		} catch (Exception e) {
			Logger.loaderDebug("Failed to load individuals", LogLevel.WARN);
		}
	}
}