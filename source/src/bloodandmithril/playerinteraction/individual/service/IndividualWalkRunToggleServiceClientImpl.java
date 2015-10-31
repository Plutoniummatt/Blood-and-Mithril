package bloodandmithril.playerinteraction.individual.service;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualWalkRunToggleService;

/**
 * Client side implementation of {@link IndividualWalkRunToggleService}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualWalkRunToggleServiceClientImpl implements IndividualWalkRunToggleService {

	@Override
	public void setWalking(Individual individual, boolean isWalking) {
		ClientServerInterface.SendRequest.sendRunWalkRequest(individual.getId().getId(), isWalking);
	}
}