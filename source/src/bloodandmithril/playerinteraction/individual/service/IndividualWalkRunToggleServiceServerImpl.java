package bloodandmithril.playerinteraction.individual.service;

import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.playerinteraction.individual.api.IndividualWalkRunToggleService;

/**
 * Server side implementation of {@link IndividualWalkRunToggleService}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class IndividualWalkRunToggleServiceServerImpl implements IndividualWalkRunToggleService {

	@Override
	public void setWalking(Individual individual, boolean isWalking) {
		individual.setWalking(isWalking);
	}
}