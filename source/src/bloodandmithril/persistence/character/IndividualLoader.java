package bloodandmithril.persistence.character;

import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.util.Logger.loaderDebug;
import static com.badlogic.gdx.Gdx.files;

import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.AddIndividualService;
import bloodandmithril.character.ai.ArtificialIntelligence.AIMode;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.PersistenceParameters;
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

	@Inject private PersistenceParameters persistenceParameters;
	@Inject private GameClientStateTracker gameClientStateTracker;
	@Inject private AddIndividualService addIndividualService;

	/**
	 * Loads all {@link Individual}s
	 */
	@SuppressWarnings("unchecked")
	public void loadAll() {
		try {
			final ConcurrentHashMap<Integer, Individual> decoded = (ConcurrentHashMap<Integer, Individual>) decode(files.local(persistenceParameters.getSavePath() + "/world/individuals.txt"));

			decoded.values().stream().forEach(individual -> {
				addIndividualService.addIndividual(individual, individual.getWorldId());
			});

			if (ClientServerInterface.isClient()) {
				for (final Individual individual : Domain.getIndividuals()) {
					if (individual.getAI().getAIMode() == AIMode.MANUAL) {
						gameClientStateTracker.addSelectedIndividual(individual);
					}
				}
			}
		} catch (final Exception e) {
			loaderDebug("Failed to load individuals", LogLevel.DEBUG);
		}
	}
}