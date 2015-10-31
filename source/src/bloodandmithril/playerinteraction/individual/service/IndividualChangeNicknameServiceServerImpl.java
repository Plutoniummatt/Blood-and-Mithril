package bloodandmithril.playerinteraction.individual.service;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.playerinteraction.individual.api.IndividualChangeNicknameService;

/**
 * Server side implementation of {@link IndividualChangeNicknameService}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualChangeNicknameServiceServerImpl implements IndividualChangeNicknameService {

	@Override
	public void changeNickname(Individual individual, String toChangeTo) {
		individual.getId().setNickName(toChangeTo);
	}
}