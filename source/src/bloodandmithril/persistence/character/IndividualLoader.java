package bloodandmithril.persistence.character;

import static bloodandmithril.persistence.GameSaver.savePath;
import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.util.Logger.loaderDebug;
import static bloodandmithril.world.Domain.setIndividuals;
import static com.badlogic.gdx.Gdx.files;

import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.util.Logger.LogLevel;

/**
 * Loads {@link Individual}s
 *
 * @author Matt
 */
public class IndividualLoader {

	/**
	 * Loads all {@link Individual}s
	 */
	@SuppressWarnings("unchecked")
	public static void loadAll() {
		try {
			ConcurrentHashMap<Integer, Individual> decoded = (ConcurrentHashMap<Integer, Individual>) decode(files.local(savePath + "/world/individuals.txt"));
			setIndividuals(decoded);
		} catch (Exception e) {
			loaderDebug("Failed to load individuals", LogLevel.WARN);
		}
	}
}