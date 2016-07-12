package bloodandmithril.control;

import static bloodandmithril.character.ai.pathfinding.PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace;
import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.isButtonPressed;
import static bloodandmithril.control.InputUtilities.isKeyPressed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.character.ai.task.MineTile;
import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.event.events.IndividualMoved;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.Function;
import bloodandmithril.util.Util;
import bloodandmithril.util.cursorboundtask.ThrowItemCursorBoundTask;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * {@link InputProcessor} for the game client
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class BloodAndMithrilClientInputProcessor implements InputProcessor {

	@Inject private Controls controls;
	@Inject	private Graphics graphics;
	@Inject	private FactionControlService factionControlService;
	@Inject	private GameSaver gameSaver;
	@Inject	private GameClientStateTracker gameClientStateTracker;
	@Inject private InputHandlers handlers;

	private CursorBoundTask cursorBoundTask = null;
	private Function<Vector2> camFollowFunction;

	/** The current timer for double clicking */
	public static long leftDoubleClickTimer = 0L;
	public static long rightDoubleClickTimer = 0L;

	@Override
	public boolean keyUp(final int keycode) {
		return false;
	}


	@Override
	public boolean keyTyped(final char character) {
		return false;
	}


	@Override
	public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
		try {
			if (button == controls.leftClick.keyCode) {
				UserInterface.leftClickRelease(screenX, graphics.getHeight() - screenY);
			}

			if (button == controls.rightClick.keyCode) {
				UserInterface.rightClickRelease(screenX, graphics.getHeight() - screenY);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}

		return false;
	}


	@Override
	public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
		if (gameSaver.isSaving()) {
			return false;
		}

		if (isButtonPressed(controls.middleClick.keyCode) && gameClientStateTracker.isInGame()) {
			graphics.getCam().position.x = Controls.oldCamX + Controls.camDragX - screenX;
			graphics.getCam().position.y = Controls.oldCamY + screenY - Controls.camDragY;
		}
		return false;
	}


	@Override
	public boolean mouseMoved(final int screenX, final int screenY) {
		return false;
	}


	@Override
	public boolean scrolled(final int amount) {
		try {
			UserInterface.scrolled(amount);
		} catch (final Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}

		return false;
	}


	@Override
	public boolean keyDown(final int keycode) {
		// Ignore input during save/load
		if (gameSaver.isSaving() || gameClientStateTracker.isLoading()) {
			return false;
		}

		handlers.iterateKeyDown(keycode);

		return false;
	}


	@Override
	public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
		try {
			if (gameSaver.isSaving() || gameClientStateTracker.isLoading()) {
				return false;
			}

			if (button == controls.leftClick.keyCode) {
				leftClick(screenX, screenY);
			}

			if (button == controls.rightClick.keyCode) {
				rightClick();
			}

			if (button == controls.middleClick.keyCode) {
				middleClick(screenX, screenY);
			}
		} catch (final NoTileFoundException e) {
		} catch (final Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}

		return false;
	}


	/**
	 * Called upon right clicking
	 */
	@SuppressWarnings("unused")
	private void rightClick() throws NoTileFoundException {
		final long currentTime = System.currentTimeMillis();
		final boolean doubleClick = rightDoubleClickTimer + Controls.DOUBLE_CLICK_TIME > currentTime;
		boolean uiClicked = false;
		rightDoubleClickTimer = currentTime;

		if (cursorBoundTask != null && cursorBoundTask.canCancel()) {
			cursorBoundTask = null;
			return;
		}

		UserInterface.initialRightMouseDragCoordinates = new Vector2(getMouseScreenX(), getMouseScreenY());

		if (isKeyPressed(controls.attack.keyCode) && !isKeyPressed(controls.rangedAttack.keyCode)) {
			meleeAttack();
		} else if (isKeyPressed(controls.rangedAttack.keyCode)) {
			rangedAttack();
		} else if (!isKeyPressed(Keys.ANY_KEY)) {
			uiClicked = UserInterface.rightClick();
		}

		if (UserInterface.contextMenus.isEmpty() && !uiClicked && !isKeyPressed(controls.rightClickDragBox.keyCode) && !isKeyPressed(controls.attack.keyCode) && !isKeyPressed(controls.rangedAttack.keyCode)) {
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
	}


	private void middleClick(final int screenX, final int screenY) {
		camFollowFunction = null;
		saveCamDragCoordinates(screenX, screenY);
	}


	private void moveIndividual(final Individual indi) throws NoTileFoundException {
		final float spread = Math.min(indi.getWidth() * (Util.getRandom().nextFloat() - 0.5f) * 0.5f * (gameClientStateTracker.getSelectedIndividuals().size() - 1), Controls.INDIVIDUAL_SPREAD);
		if (ClientServerInterface.isServer()) {
			AIProcessor.sendPathfindingRequest(
				indi,
				new WayPoint(
					Topography.convertToWorldCoord(
						getGroundAboveOrBelowClosestEmptyOrPlatformSpace(
							new Vector2(
								getMouseWorldX() + (isKeyPressed(controls.forceMove.keyCode) ? 0f : spread),
								getMouseWorldY()
							),
							10,
							Domain.getWorld(indi.getWorldId())
						),
						true
					)
				),
				false,
				150f,
				!isKeyPressed(controls.forceMove.keyCode),
				isKeyPressed(controls.addWayPoint.keyCode)
			);

			Domain.getWorld(indi.getWorldId()).addEvent(new IndividualMoved(indi));
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
					),
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
			indi.getAI().setCurrentTask(new MineTile(indi, mouseCoordinate));
		} else {
			ClientServerInterface.SendRequest.sendMineTileRequest(indi.getId().getId(), new Vector2(getMouseWorldX(), getMouseWorldY()));
		}
	}


	private void rangedAttack() {
		for (final Individual selected : gameClientStateTracker.getSelectedIndividuals()) {
			if (selected.canAttackRanged()) {
				if (ClientServerInterface.isServer()) {
					selected.attackRanged(new Vector2(getMouseWorldX(), getMouseWorldY()));
				} else {
					ClientServerInterface.SendRequest.sendAttackRangedRequest(selected, new Vector2(getMouseWorldX(), getMouseWorldY()));
				}
			}
		}
	}


	private void meleeAttack() {
		if (!gameClientStateTracker.getSelectedIndividuals().isEmpty()) {
			for (final int indiKey : gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
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
					break;
				}
			}
		}
	}


	/**
	 * Called upon left clicking
	 */
	private void leftClick(final int screenX, final int screenY) {
		if (gameSaver.isSaving()) {
			return;
		}

		final long currentTimeMillis = System.currentTimeMillis();
		final boolean doubleClick = leftDoubleClickTimer + Controls.DOUBLE_CLICK_TIME > currentTimeMillis;
		leftDoubleClickTimer = currentTimeMillis;

		final boolean uiClicked = graphics.getUi().leftClick();

		Individual individualClicked = null;
		if (gameClientStateTracker.getActiveWorld() != null) {
			for (final int indiKey : gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
				final Individual indi = Domain.getIndividual(indiKey);
				if (indi.isMouseOver()) {
					individualClicked = indi;
				}
			}
		}

		if (!uiClicked && gameClientStateTracker.isInGame()) {
			if (getCursorBoundTask() != null) {
				if (getCursorBoundTask().executionConditionMet()) {
					if (getCursorBoundTask().isWorldCoordinate()) {
						setCursorBoundTask(getCursorBoundTask().execute(
							(int) getMouseWorldX(),
							(int) getMouseWorldY()
						));
					} else {
						setCursorBoundTask(getCursorBoundTask().execute(
							getMouseScreenX(),
							getMouseScreenY()
						));
					}
				}
				return;
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
	}


	/**
	 * Camera movement controls
	 */
	public void cameraControl() {
		if (camFollowFunction != null) {
			final Vector2 followCam = camFollowFunction.call();
			graphics.getCam().position.x += (followCam.x - graphics.getCam().position.x) * 0.01f;
			graphics.getCam().position.y += (followCam.y - graphics.getCam().position.y) * 0.03f;
		}

		if (!isKeyPressed(Keys.CONTROL_LEFT) && gameClientStateTracker.isInGame()) {

			if (isKeyPressed(controls.moveCamUp.keyCode)){
				graphics.getCam().position.y += 10f;
				this.camFollowFunction = null;
			}
			if (isKeyPressed(controls.moveCamDown.keyCode)){
				graphics.getCam().position.y -= 10f;
				this.camFollowFunction = null;
			}
			if (isKeyPressed(controls.moveCamLeft.keyCode)){
				graphics.getCam().position.x -= 10f;
				this.camFollowFunction = null;
			}
			if (isKeyPressed(controls.moveCamRight.keyCode)){
				graphics.getCam().position.x += 10f;
				this.camFollowFunction = null;
			}
		}
	}


	/**
	 * Camera dragging processing
	 */
	private void saveCamDragCoordinates(final int screenX, final int screenY) {
		Controls.oldCamX = (int)graphics.getCam().position.x;
		Controls.oldCamY = (int)graphics.getCam().position.y;

		Controls.camDragX = screenX;
		Controls.camDragY = screenY;
	}


	/**
	 * @return the active {@link CursorBoundTask}
	 */
	public CursorBoundTask getCursorBoundTask() {
		return cursorBoundTask;
	}


	/**
	 * @param cursorBoundTask to set
	 */
	public void setCursorBoundTask(final CursorBoundTask cursorBoundTask) {
		this.cursorBoundTask = cursorBoundTask;
	}


	public Controls getKeyMappings() {
		return controls;
	}


	public void setCamFollowFunction(final Function<Vector2> camFollowFunction) {
		this.camFollowFunction = camFollowFunction;
	}
}