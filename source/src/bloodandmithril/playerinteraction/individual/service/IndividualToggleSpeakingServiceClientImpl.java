package bloodandmithril.playerinteraction.individual.service;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualToggleSpeakingService;

/**
 * Server side implementation of {@link IndividualToggleSpeakingService}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualToggleSpeakingServiceClientImpl implements IndividualToggleSpeakingService {

	@Override
	public void setSpeaking(Individual individual, boolean speaking) {
		ClientServerInterface.SendRequest.sendIndividualSpeakRequest(individual, speaking);
	}
}