package bloodandmithril.persistence.character;

import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.util.Logger.loaderDebug;
import static bloodandmithril.world.Domain.setIndividuals;
import static com.badlogic.gdx.Gdx.files;

import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.ArtificialIntelligence.AIMode;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;

/**
 * Loads {@link Individual}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class IndividualLoader {

	@Inject private GameSaver gameSaver;

	/**
	 * Loads all {@link Individual}s
	 */
	@SuppressWarnings("unchecked")
	public void loadAll() {
		try {
			ConcurrentHashMap<Integer, Individual> decoded = (ConcurrentHashMap<Integer, Individual>) decode(files.local(gameSaver.getSavePath() + "/world/individuals.txt"));
			setIndividuals(decoded);
			if (ClientServerInterface.isClient()) {
				for (Individual individual : Domain.getIndividuals().values()) {
					if (individual.getAI().getAIMode() == AIMode.MANUAL) {
						Domain.addSelectedIndividual(individual);
					}
				}
			}
		} catch (Exception e) {
			loaderDebug("Failed to load individuals", LogLevel.DEBUG);
		}
	}
}