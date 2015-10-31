package bloodandmithril.playerinteraction.individual.api;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Service for instructing an individual to trade with another
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface IndividualTradeWithOtherService {

	public void tradeWith(final Individual proposer, final Individual proposee);
}