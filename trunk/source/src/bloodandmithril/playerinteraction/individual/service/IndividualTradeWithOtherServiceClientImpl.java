package bloodandmithril.playerinteraction.individual.service;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualTradeWithOtherService;

/**
 * Client side implementation of {@link IndividualTradeWithOtherService}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualTradeWithOtherServiceClientImpl implements IndividualTradeWithOtherService {

	@Override
	public void tradeWith(Individual proposer, Individual proposee) {
		ClientServerInterface.SendRequest.sendTradeWithIndividualRequest(proposer, proposee);
	}
}