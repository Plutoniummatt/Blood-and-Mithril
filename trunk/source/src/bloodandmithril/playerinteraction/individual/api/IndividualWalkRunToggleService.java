package bloodandmithril.playerinteraction.individual.api;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Service for selecting and deselecting individuals
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface IndividualWalkRunToggleService {

	/**
	 * Sets whether or not an individual is walking
	 */
	public void setWalking(Individual individual, boolean isWalking);
}