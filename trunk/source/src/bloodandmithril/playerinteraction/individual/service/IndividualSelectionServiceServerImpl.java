package bloodandmithril.playerinteraction.individual.service;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;
import bloodandmithril.world.Domain;

/**
 * See {@link IndividualSelectionService}, this implementation is used for a game server, or a client running in server mode
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualSelectionServiceServerImpl implements IndividualSelectionService {


	@Override
	public void select(Individual indi) {
		indi.select(0);
	}


	@Override
	public void deselect(Individual indi) {
		indi.deselect(false, 0);
		Domain.removeSelectedIndividual(indi);
		indi.clearCommands();
	}
}