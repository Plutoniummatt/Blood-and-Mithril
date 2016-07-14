package bloodandmithril.persistence.character;

import static bloodandmithril.persistence.PersistenceUtil.encode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.persistence.PersistenceParameters;
import bloodandmithril.world.Domain;

/**
 * Saves {@link Individual}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class IndividualSaver {

	@Inject private PersistenceParameters persistenceParameters;

	/**
	 * Saves all {@link Individual}s
	 */
	public void saveAll() {
		final FileHandle individuals = Gdx.files.local(persistenceParameters.getSavePath() + "/world/individuals.txt");
		individuals.writeString(encode(Domain.getIndividualsMap()), false);
	}
}