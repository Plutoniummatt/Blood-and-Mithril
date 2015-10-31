package bloodandmithril.playerinteraction.individual.service;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualUpdateDescriptionService;

/**
 * Client side implementation of {@link IndividualUpdateDescriptionService}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualUpdateDescriptionServiceClientImpl implements IndividualUpdateDescriptionService {

	@Override
	public void updateDescription(Individual individual, String toChangeTo) {
		ClientServerInterface.SendRequest.sendUpdateBiographyRequest(individual, toChangeTo);
	}
}