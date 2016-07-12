package bloodandmithril.playerinteraction.individual.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;

/**
 * See {@link IndividualSelectionService}, this implementation is used for a game server, or a client running in server mode
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class IndividualSelectionServiceServerImpl implements IndividualSelectionService {

	@Inject
	private GameClientStateTracker gameClientStateTracker;


	@Override
	public void select(final Individual indi) {
		indi.select(0);
	}


	@Override
	public void deselect(final Individual indi) {
		indi.deselect(false, 0);
		gameClientStateTracker.removeSelectedIndividual(indi);
		indi.clearCommands();
	}
}