package bloodandmithril.playerinteraction.individual.service;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;

/**
 * See {@link IndividualSelectionService}, this implementation is used for game client connected to a remote server
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualSelectionServiceClientImpl implements IndividualSelectionService {


	@Override
	public void select(Individual indi) {
		ClientServerInterface.SendRequest.sendIndividualSelectionRequest(indi.getId().getId(), true);
	}


	@Override
	public void deselect(Individual indi) {
		ClientServerInterface.SendRequest.sendIndividualSelectionRequest(indi.getId().getId(), false);
	}
}