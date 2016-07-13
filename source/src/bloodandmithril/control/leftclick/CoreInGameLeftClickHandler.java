package bloodandmithril.control.leftclick;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.isKeyPressed;

import com.google.inject.Inject;

import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.control.Controls;
import bloodandmithril.control.LeftClickHandler;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.cursorboundtask.ThrowItemCursorBoundTask;
import bloodandmithril.world.Domain;

/**
 * {@link LeftClickHandler} for in-game controls
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class CoreInGameLeftClickHandler implements LeftClickHandler {

	@Inject
	private GameClientStateTracker gameClientStateTracker;
	@Inject
	private BloodAndMithrilClientInputProcessor inputProcessor;
	@Inject
	private FactionControlService factionControlService;
	@Inject
	private Controls controls;

	@Override
	public boolean leftClick(final boolean doubleClick) {
		if (gameClientStateTracker.isInGame()) {
			final CursorBoundTask cursorBoundTask = inputProcessor.getCursorBoundTask();
			if (cursorBoundTask != null) {
				if (cursorBoundTask.executionConditionMet()) {
					if (cursorBoundTask.isWorldCoordinate()) {
						inputProcessor.setCursorBoundTask(cursorBoundTask.execute(
							(int) getMouseWorldX(),
							(int) getMouseWorldY()
						));
					} else {
						inputProcessor.setCursorBoundTask(cursorBoundTask.execute(
							getMouseScreenX(),
							getMouseScreenY()
						));
					}
				}
				return true;
			}

			Individual individualClicked = null;
			if (gameClientStateTracker.getActiveWorld() != null) {
				for (final int indiKey : gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
					final Individual indi = Domain.getIndividual(indiKey);
					if (indi.isMouseOver()) {
						individualClicked = indi;
					}
				}
			}

			final IndividualSelectionService individualSelectionService = Wiring.injector().getInstance(IndividualSelectionService.class);
			if (individualClicked == null) {
				if (doubleClick && (cursorBoundTask == null || !(cursorBoundTask instanceof ThrowItemCursorBoundTask))) {
					for (final Individual indi : Domain.getIndividuals().values()) {
						if (factionControlService.isControllable(indi)) {
							individualSelectionService.deselect(indi);
						}
					}
					if (ClientServerInterface.isServer()) {
						gameClientStateTracker.clearSelectedIndividuals();
					}
				}
			} else {
				for (final Individual indi : Domain.getIndividuals().values()) {
					if (factionControlService.isControllable(indi) && indi.getId().getId() != individualClicked.getId().getId() && !isKeyPressed(controls.selectIndividual.keyCode)) {
						individualSelectionService.deselect(indi);
					}
				}

				if (factionControlService.isControllable(individualClicked) && individualClicked.isAlive()) {
					individualSelectionService.select(individualClicked, ClientServerInterface.getClientID());
				}
			}
		}

		return false;
	}
}