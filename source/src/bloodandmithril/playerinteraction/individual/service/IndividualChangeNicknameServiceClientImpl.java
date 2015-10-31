package bloodandmithril.playerinteraction.individual.service;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualChangeNicknameService;

/**
 * Client side implementation of {@link IndividualChangeNicknameService}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualChangeNicknameServiceClientImpl implements IndividualChangeNicknameService {

	@Override
	public void changeNickname(Individual individual, String toChangeTo) {
		ClientServerInterface.SendRequest.sendChangeNickNameRequest(individual.getId().getId(), toChangeTo);
	}
}