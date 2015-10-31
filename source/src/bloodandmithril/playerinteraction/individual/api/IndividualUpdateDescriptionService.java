package bloodandmithril.playerinteraction.individual.api;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Service for changing the description of an {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface IndividualUpdateDescriptionService {

	public void updateDescription(Individual individual, String toChangeTo);
}