package bloodandmithril.playerinteraction.individual.service;

import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.playerinteraction.individual.api.IndividualChangeNicknameService;

/**
 * Server side implementation of {@link IndividualChangeNicknameService}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class IndividualChangeNicknameServiceServerImpl implements IndividualChangeNicknameService {

	@Override
	public void changeNickname(Individual individual, String toChangeTo) {
		individual.getId().setNickName(toChangeTo);
	}
}