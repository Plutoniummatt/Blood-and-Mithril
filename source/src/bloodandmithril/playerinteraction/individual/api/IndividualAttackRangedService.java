package bloodandmithril.playerinteraction.individual.api;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Service for ranged attacks by {@link Individual}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface IndividualAttackRangedService {

	/**
	 * Commands the {@link Individual} to shoot in the specified direction
	 *
	 * @param individual
	 * @param direction
	 */
	public void attack(Individual individual, Vector2 direction);
}