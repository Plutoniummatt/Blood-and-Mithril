package bloodandmithril.playerinteraction.individual.service;

import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.playerinteraction.individual.api.IndividualAISupressionService;

/**
 * See {@link IndividualAISupressionService}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class IndividualAISupressionServiceServerImpl implements IndividualAISupressionService {

	@Override
	public void setAIsupression(Individual indi, boolean supress) {
		indi.setAISuppression(supress);
	}
}