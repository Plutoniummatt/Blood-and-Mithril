package bloodandmithril.playerinteraction.individual.service;

import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.playerinteraction.individual.api.IndividualTradeWithOtherService;

/**
 * Server side implementation of {@link IndividualTradeWithOtherService}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualTradeWithOtherServiceServerImpl implements IndividualTradeWithOtherService {

	@Override
	public void tradeWith(Individual proposer, Individual proposee) {
		proposer.getAI().setCurrentTask(
			new TradeWith(proposer, proposee)
		);
	}
}