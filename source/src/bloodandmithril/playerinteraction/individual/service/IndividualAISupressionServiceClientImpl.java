package bloodandmithril.playerinteraction.individual.service;

import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualAISupressionService;

/**
 * See {@link IndividualAISupressionService}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@Singleton
public class IndividualAISupressionServiceClientImpl implements IndividualAISupressionService {

	@Override
	public void setAIsupression(Individual indi, boolean supress) {
		ClientServerInterface.SendRequest.sendAISuppressionRequest(indi, supress);
	}
}