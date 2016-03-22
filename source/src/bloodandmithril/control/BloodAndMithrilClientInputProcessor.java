package bloodandmithril.control;

import static bloodandmithril.character.ai.pathfinding.PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace;
import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.isButtonPressed;
import static bloodandmithril.control.InputUtilities.isKeyPressed;
import static bloodandmithril.core.BloodAndMithrilClient.isInGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.event.events.IndividualMoved;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.DevWindow;
import bloodandmithril.ui.components.window.MainMenuWindow;
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

	private CursorBoundTask cursorBoundTask = null;
	private Function<Vector2> camFollowFunction;


	@Override
	public boolean keyUp(int keycode) {
		return false;
	}


	@Override
	public boolean keyTyped(char character) {
		return false;
	}


	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		try {
			if (button == controls.leftClick.keyCode) {
				UserInterface.leftClickRelease(screenX, graphics.getHeight() - screenY);
			}

			if (button == controls.rightClick.keyCode) {
				UserInterface.rightClickRelease(screenX, graphics.getHeight() - screenY);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}

		return false;
	}


	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (gameSaver.isSaving()) {
			return false;
		}

		if (isButtonPressed(controls.middleClick.keyCode) && isInGame()) {
			graphics.getCam().position.x = Controls.oldCamX + Controls.camDragX - screenX;
			graphics.getCam().position.y = Controls.oldCamY + screenY - Controls.camDragY;
		}
		return false;
	}


	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}


	@Override
	public boolean scrolled(int amount) {
		try {
			UserInterface.scrolled(amount);
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}

		return false;
	}


	@Override
	public boolean keyDown(int keycode) {
		if (isKeyPressed(Keys.CONTROL_LEFT) && keycode == Input.Keys.D) {
			UserInterface.addLayeredComponentUnique(
				new DevWindow(
					graphics.getWidth(),
					graphics.getHeight()/2 + 150,
					500,
					300,
					true
				)
			);
		}

		try {
			if (gameSaver.isSaving() || BloodAndMithrilClient.loading.get()) {
				return false;
			}

			if (UserInterface.keyPressed(keycode)) {
				return false;
			} else if (Keys.ESCAPE == keycode) {
				UserInterface.addLayeredComponentUnique(
					new MainMenuWindow(true)
				);
			} else {
				if (keycode == controls.speedUp.keyCode) {
					if (BloodAndMithrilClient.updateRateMultiplier < 16f) {
						BloodAndMithrilClient.updateRateMultiplier = Math.round(BloodAndMithrilClient.updateRateMultiplier) + 1;
					}
				}
				if (keycode == controls.slowDown.keyCode) {
					if (BloodAndMithrilClient.updateRateMultiplier > 1f) {
						BloodAndMithrilClient.updateRateMultiplier = Math.round(BloodAndMithrilClient.updateRateMultiplier) - 1;
					}
				}
				if (cursorBoundTask != null) {
					cursorBoundTask.keyPressed(keycode);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}

		return false;
	}


	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		try {
			if (gameSaver.isSaving() || BloodAndMithrilClient.loading.get()) {
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
		} catch (NoTileFoundException e) {
		} catch (Exception e) {
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
		long currentTime = System.currentTimeMillis();
		boolean doubleClick = Controls.rightDoubleClickTimer + Controls.DOUBLE_CLICK_TIME > currentTime;
		boolean uiClicked = false;
		Controls.rightDoubleClickTimer = currentTime;

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
			Vector2 mouseCoordinate = new Vector2(getMouseWorldX(), getMouseWorldY());
			for (Individual indi : Sets.newHashSet(Domain.getSelectedIndividuals())) {
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


	private void middleClick(int screenX, int screenY) {
		camFollowFunction = null;
		saveCamDragCoordinates(screenX, screenY);
	}


	private void moveIndividual(Individual indi) throws NoTileFoundException {
		float spread = Math.min(indi.getWidth() * (Util.getRandom().nextFloat() - 0.5f) * 0.5f * (Domain.getSelectedIndividuals().size() - 1), Controls.INDIVIDUAL_SPREAD);
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


	private void jump(Individual indi) {
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


	private void mineTile(Vector2 mouseCoordinate, Individual indi) {
		if (ClientServerInterface.isServer()) {
			indi.getAI().setCurrentTask(new MineTile(indi, mouseCoordinate));
		} else {
			ClientServerInterface.SendRequest.sendMineTileRequest(indi.getId().getId(), new Vector2(getMouseWorldX(), getMouseWorldY()));
		}
	}


	private void rangedAttack() {
		for (Individual selected : Domain.getSelectedIndividuals()) {
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
		if (!Domain.getSelectedIndividuals().isEmpty()) {
			for (final int indiKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
				Individual indi = Domain.getIndividual(indiKey);
				if (indi.isMouseOver() && indi.isAlive()) {
					for (Individual selected : Domain.getSelectedIndividuals()) {
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
	private void leftClick(int screenX, int screenY) {

		long currentTimeMillis = System.currentTimeMillis();

		boolean doubleClick = Controls.leftDoubleClickTimer + Controls.DOUBLE_CLICK_TIME > currentTimeMillis;
		Controls.leftDoubleClickTimer = currentTimeMillis;

		boolean uiClicked = UserInterface.leftClick();

		Individual individualClicked = null;
		if (Domain.getActiveWorld() != null) {
			for (int indiKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
				Individual indi = Domain.getIndividual(indiKey);
				if (indi.isMouseOver()) {
					individualClicked = indi;
				}
			}
		}

		if (!uiClicked && isInGame()) {
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

			IndividualSelectionService individualSelectionService = Wiring.injector().getInstance(IndividualSelectionService.class);
			if (individualClicked == null) {
				if (doubleClick && (cursorBoundTask == null || !(cursorBoundTask instanceof ThrowItemCursorBoundTask))) {
					for (Individual indi : Domain.getIndividuals().values()) {
						if (factionControlService.isControllable(indi)) {
							individualSelectionService.deselect(indi);
						}
					}
					if (ClientServerInterface.isServer()) {
						Domain.clearSelectedIndividuals();
					}
				}
			} else {
				for (Individual indi : Domain.getIndividuals().values()) {
					if (factionControlService.isControllable(indi) && indi.getId().getId() != individualClicked.getId().getId() && !isKeyPressed(controls.selectIndividual.keyCode)) {
						individualSelectionService.deselect(indi);
					}
				}

				if (factionControlService.isControllable(individualClicked) && individualClicked.isAlive()) {
					individualSelectionService.select(individualClicked);
				}

			}
		}
	}


	/**
	 * Camera movement controls
	 */
	public void cameraControl() {
		if (camFollowFunction != null) {
			Vector2 followCam = camFollowFunction.call();
			graphics.getCam().position.x += (followCam.x - graphics.getCam().position.x) * 0.01f;
			graphics.getCam().position.y += (followCam.y - graphics.getCam().position.y) * 0.03f;
		}

		if (!isKeyPressed(Keys.CONTROL_LEFT) && isInGame()) {

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
	private void saveCamDragCoordinates(int screenX, int screenY) {
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
	public void setCursorBoundTask(CursorBoundTask cursorBoundTask) {
		this.cursorBoundTask = cursorBoundTask;
	}


	public Controls getKeyMappings() {
		return controls;
	}


	public void setCamFollowFunction(Function<Vector2> camFollowFunction) {
		this.camFollowFunction = camFollowFunction;
	}
}