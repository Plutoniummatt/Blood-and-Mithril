package bloodandmithril.control;

import static bloodandmithril.control.InputUtilities.isButtonPressed;
import static bloodandmithril.control.InputUtilities.isKeyPressed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.Function;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

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

		handlers.keyDown(keycode);

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
		final boolean uiClicked = false;
		rightDoubleClickTimer = currentTime;
	}


	private void middleClick(final int screenX, final int screenY) {
		camFollowFunction = null;
		saveCamDragCoordinates(screenX, screenY);
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

		handlers.leftClick(doubleClick);
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