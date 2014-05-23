package bloodandmithril.persistence.character;

import static bloodandmithril.persistence.PersistenceUtil.encode;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Saves {@link Individual}s
 *
 * @author Matt
 */
public class IndividualSaver {

	/**
	 * Saves all {@link Individual}s
	 */
	public static void saveAll() {
		FileHandle individuals = Gdx.files.local(GameSaver.savePath + "/world/individuals.txt");
		individuals.writeString(encode(Domain.getIndividuals()), false);
	}
}