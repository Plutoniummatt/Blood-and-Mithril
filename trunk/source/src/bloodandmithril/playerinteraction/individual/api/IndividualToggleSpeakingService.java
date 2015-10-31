package bloodandmithril.playerinteraction.individual.api;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Service for toggling speech for {@link Individual}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface IndividualToggleSpeakingService {

	public void setSpeaking(Individual individual, boolean speaking);
}