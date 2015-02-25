package bloodandmithril.persistence.character;

import static bloodandmithril.persistence.GameSaver.getSavePath;
import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.util.Logger.loaderDebug;
import static bloodandmithril.world.Domain.setIndividuals;
import static com.badlogic.gdx.Gdx.files;

import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.character.ai.ArtificialIntelligence.AIMode;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;

/**
 * Loads {@link Individual}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class IndividualLoader {

	/**
	 * Loads all {@link Individual}s
	 */
	@SuppressWarnings("unchecked")
	public static void loadAll() {
		try {
			ConcurrentHashMap<Integer, Individual> decoded = (ConcurrentHashMap<Integer, Individual>) decode(files.local(getSavePath() + "/world/individuals.txt"));
			setIndividuals(decoded);
			if (ClientServerInterface.isClient()) {
				for (Individual individual : Domain.getIndividuals().values()) {
					if (individual.getAI().getAIMode() == AIMode.MANUAL) {
						Domain.getSelectedIndividuals().add(individual);
					}
				}
			}
		} catch (Exception e) {
			loaderDebug("Failed to load individuals", LogLevel.DEBUG);
		}
	}
}