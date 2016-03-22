package bloodandmithril.persistence.character;

import static bloodandmithril.persistence.PersistenceUtil.encode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.world.Domain;

/**
 * Saves {@link Individual}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class IndividualSaver {

	@Inject private GameSaver gameSaver;

	/**
	 * Saves all {@link Individual}s
	 */
	public void saveAll() {
		FileHandle individuals = Gdx.files.local(gameSaver.getSavePath() + "/world/individuals.txt");
		individuals.writeString(encode(Domain.getIndividuals()), false);
	}
}