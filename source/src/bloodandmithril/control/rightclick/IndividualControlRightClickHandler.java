package bloodandmithril.control.rightclick;

import static bloodandmithril.character.ai.pathfinding.PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.isKeyPressed;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.ImpossibleToSetTaskException;
import bloodandmithril.character.ai.task.attack.Attack;
import bloodandmithril.character.ai.task.minetile.MineTile;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.Controls;
import bloodandmithril.control.RightClickHandler;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.event.events.IndividualMoved;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualAttackRangedService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

@Singleton
@Copyright("Matthew Peck 2016")
public class IndividualControlRightClickHandler implements RightClickHandler {

	@Inject	private Controls controls;
	@Inject	private GameClientStateTracker gameClientStateTracker;
	@Inject	private UserInterface userInterface;
	@Inject	private IndividualAttackRangedService individualAttackRangedService;

	@Override
	public boolean rightClick(final boolean doubleClick) {
		try {

			if (isKeyPressed(controls.attack.keyCode) && !isKeyPressed(controls.rangedAttack.keyCode)) {
				if (meleeAttack()) {
					return true;
				}
			} else if (isKeyPressed(controls.rangedAttack.keyCode)) {
				rangedAttack();
			}


			if (userInterface.getContextMenus().isEmpty() && !isKeyPressed(controls.rightClickDragBox.keyCode) && !isKeyPressed(controls.attack.keyCode) && !isKeyPressed(controls.rangedAttack.keyCode)) {
				final Vector2 mouseCoordinate = new Vector2(getMouseWorldX(), getMouseWorldY());
				for (final Individual indi : Sets.newHashSet(gameClientStateTracker.getSelectedIndividuals())) {
					if (isKeyPressed(controls.mineTile.keyCode) && !Domain.getWorld(indi.getWorldId()).getTopography().getTile(mouseCoordinate, true).getClass().equals(EmptyTile.class)) {
						mineTile(mouseCoordinate, indi);
					} else if (isKeyPressed(controls.jump.keyCode)) {
						jump(indi);
					} else {
						moveIndividual(indi);
					}
				}
			}
		} catch (final NoTileFoundException e) {
			// ???
		}

		return false;
	}


	private void moveIndividual(final Individual indi) throws NoTileFoundException {
		final float spread = Math.min(indi.getWidth() * (Util.getRandom().nextFloat() - 0.5f) * 0.5f * (gameClientStateTracker.getSelectedIndividuals().size() - 1), Controls.INDIVIDUAL_SPREAD);
		if (ClientServerInterface.isServer()) {
			Optional<Vector2> groundAboveOrBelowClosestEmptyOrPlatformSpace = getGroundAboveOrBelowClosestEmptyOrPlatformSpace(
				new Vector2(
					getMouseWorldX() + (isKeyPressed(controls.forceMove.keyCode) ? 0f : spread),
					getMouseWorldY()
				),
				10,
				Domain.getWorld(indi.getWorldId())
			);
			
			if (groundAboveOrBelowClosestEmptyOrPlatformSpace.isPresent()) {
				AIProcessor.sendPathfindingRequest(
					indi,
					new WayPoint(
						Topography.convertToWorldCoord(
							groundAboveOrBelowClosestEmptyOrPlatformSpace.get(),
							true
						)
					),
					false,
					150f,
					!isKeyPressed(controls.forceMove.keyCode),
					isKeyPressed(controls.addWayPoint.keyCode)
				);
				Domain.getWorld(indi.getWorldId()).addEvent(new IndividualMoved(indi));
			}
		} else {
			ClientServerInterface.SendRequest.sendMoveIndividualRequest(
				indi.getId().getId(),
				Topography.convertToWorldCoord(
					getGroundAboveOrBelowClosestEmptyOrPlatformSpace(
						new Vector2(
							getMouseWorldX() + (isKeyPressed(controls.forceMove.keyCode) ? 0f : spread),
							getMouseWorldY()
						),
						10,
						Domain.getWorld(indi.getWorldId())
					).get(),
					true
				),
				!isKeyPressed(controls.forceMove.keyCode),
				isKeyPressed(controls.addWayPoint.keyCode),
				false, null, null
			);
		}
	}


	private void jump(final Individual indi) {
		if (ClientServerInterface.isServer()) {
			AIProcessor.sendJumpResolutionRequest(
				indi,
				indi.getState().position.cpy(),
				new Vector2(getMouseWorldX(), getMouseWorldY()),
				isKeyPressed(controls.addWayPoint.keyCode)
			);
		} else {
			ClientServerInterface.SendRequest.sendMoveIndividualRequest(
				indi.getId().getId(),
				null,
				!isKeyPressed(controls.forceMove.keyCode),
				isKeyPressed(controls.addWayPoint.keyCode),
				true,
				indi.getState().position.cpy(),
				new Vector2(getMouseWorldX(), getMouseWorldY())
			);
		}
	}


	private void mineTile(final Vector2 mouseCoordinate, final Individual indi) {
		if (ClientServerInterface.isServer()) {
			try {
				indi.getAI().setCurrentTask(new MineTile(indi, mouseCoordinate));
			} catch (ImpossibleToSetTaskException e) {}
		} else {
			ClientServerInterface.SendRequest.sendMineTileRequest(indi.getId().getId(), new Vector2(getMouseWorldX(), getMouseWorldY()));
		}
	}


	private boolean meleeAttack() {
		if (!gameClientStateTracker.getSelectedIndividuals().isEmpty()) {
			for (final int indiKey : gameClientStateTracker.getActiveWorld().getPositionalIndexChunkMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
				final Individual indi = Domain.getIndividual(indiKey);
				if (indi.isMouseOver() && indi.isAlive()) {
					for (final Individual selected : gameClientStateTracker.getSelectedIndividuals()) {
						if (indi == selected) {
							continue;
						}

						if (ClientServerInterface.isServer()) {
							selected.getAI().setCurrentTask(new Attack(selected, indi));
						} else {
							ClientServerInterface.SendRequest.sendRequestAttack(selected, indi);
						}
					}
					return true;
				}
			}
		}
		return false;
	}


	private void rangedAttack() {
		for (final Individual selected : gameClientStateTracker.getSelectedIndividuals()) {
			if (selected.canAttackRanged()) {
				individualAttackRangedService.attack(selected, new Vector2(getMouseWorldX(), getMouseWorldY()));
			}
		}
	}
}