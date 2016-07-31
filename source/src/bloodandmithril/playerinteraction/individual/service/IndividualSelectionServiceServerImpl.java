package bloodandmithril.playerinteraction.individual.service;

import static bloodandmithril.graphics.Graphics.getGdxWidth;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.SelectedIndividualsControlWindow;

/**
 * See {@link IndividualSelectionService}, this implementation is used for a game server, or a client running in server mode
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class IndividualSelectionServiceServerImpl implements IndividualSelectionService {

	@Inject	private GameClientStateTracker gameClientStateTracker;
	@Inject	private UserInterface userInterface;


	@Override
	public void select(final Individual indi, final int client) {
		final GameClientStateTracker gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);

		if (!indi.isAlive()) {
			return;
		}

		gameClientStateTracker.addSelectedIndividual(indi);
		indi.getAI().setToManual();
		indi.getSelectedByClient().add(client);

		if (ClientServerInterface.isClient()) {
			userInterface.addLayeredComponentUnique(
				new SelectedIndividualsControlWindow(
					getGdxWidth() - 170,
					150,
					150,
					100,
					"Actions",
					true
				)
			);
		}
	}


	@Override
	public void deselect(final Individual indi) {
		indi.deselect(false, 0);
		gameClientStateTracker.removeSelectedIndividual(indi);
	}
}