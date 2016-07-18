package bloodandmithril.character;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.UnitsWindow;
import bloodandmithril.world.Domain;

/**
 * Service for adding individuals to the game
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class AddIndividualService {

	@Inject
	private UserInterface userInterface;

	/**
	 * Adds an individual to a world
	 */
	public void addIndividual(final Individual indi, final int worldId) {
		indi.setWorldId(worldId);
		indi.getId().getBirthday().year = Domain.getWorld(worldId).getEpoch().year;
		Domain.addIndividual(indi);
		Domain.getWorld(worldId).getIndividuals().add(indi.getId().getId());
		if (ClientServerInterface.isClient()) {
			for (final Component component : userInterface.getLayeredComponents()) {
				if (component instanceof UnitsWindow) {
					((UnitsWindow) component).refresh();
				}
			}
		} else {
			ClientServerInterface.SendNotification.notifyRefreshWindows();
		}
	}
}
