package bloodandmithril.playerinteraction.individual.api;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Service for selecting and deselecting individuals
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface IndividualSelectionService {

	/**
	 * Selects an individual
	 */
	public void select(Individual indi, int client);

	/**
	 * De-selects an individual
	 */
	public void deselect(Individual indi);
}