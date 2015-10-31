package bloodandmithril.playerinteraction.individual.api;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Service to instruct an {@link Individual} to attack another
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface IndividualAttackOtherService {

	public void attack(Individual attacker, Individual... victim);
}