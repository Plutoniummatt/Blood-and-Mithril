package bloodandmithril.playerinteraction.individual.service;

import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.playerinteraction.individual.api.IndividualUpdateDescriptionService;

/**
 * Server side implementation of {@link IndividualUpdateDescriptionService}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class IndividualUpdateDescriptionServiceServerImpl implements IndividualUpdateDescriptionService {

	@Override
	public void updateDescription(Individual individual, String toChangeTo) {
		individual.updateDescription(toChangeTo);
	}
}