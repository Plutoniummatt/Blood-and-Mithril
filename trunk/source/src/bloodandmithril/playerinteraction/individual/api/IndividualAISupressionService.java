package bloodandmithril.playerinteraction.individual.api;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Service for suppressing and un-suppressing individual AI
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface IndividualAISupressionService {

	/**
	 * Selects an individual
	 */
	public void setAIsupression(Individual indi, boolean supress);
}
