package bloodandmithril.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;

/**
 * Provides an input interface for the game
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class InputUtilities {


	/**
	 * @param processor to set
	 */
	public static void setInputProcessor(InputProcessor processor) {
		Gdx.input.setInputProcessor(processor);
	}


	/**
	 * @param keycode to check
	 * @return whether or not the key is currently pressed
	 */
	public static boolean isKeyPressed(int keycode) {
		return Gdx.input.isKeyPressed(keycode);
	}


	/**
	 * @param buttonCode to check
	 * @return whether or not the button (mouse key) is currently pressed
	 */
	public static boolean isButtonPressed(int buttonCode) {
		return Gdx.input.isButtonPressed(buttonCode);
	}



	/**
	 * @return mouse screen x-coordinate
	 */
	public static int getMouseScreenX() {
		return Gdx.input.getX();
	}


	/**
	 * @return mouse screen y-coordinate
	 */
	public static int getMouseScreenY() {
		return getGraphics().getHeight() - Gdx.input.getY();
	}


	/**
	 * Get mouse world coord X
	 */
	public static float getMouseWorldX() {
		return screenToWorldX(Gdx.input.getX());
	}


	/**
	 * Get mouse world coord y
	 */
	public static float getMouseWorldY() {
		return screenToWorldY(getGraphics().getHeight() - Gdx.input.getY());
	}


	/**
	 * Converts screen coordinates to world coordinates
	 */
	public static float screenToWorldX(float screenX) {
		return getGraphics().getCam().position.x - getGraphics().getWidth()/2 + screenX;
	}


	/**
	 * Converts screen coordinates to world coordinates
	 */
	public static float screenToWorldY(float screenY) {
		return getGraphics().getCam().position.y - getGraphics().getHeight()/2 + screenY;
	}


	/**
	 * Converts world coordinates to screen coordinates
	 */
	public static float worldToScreenX(float worldX) {
		return getGraphics().getWidth()/2 + (worldX - getGraphics().getCam().position.x);
	}


	/**
	 * Converts world coordinates to screen coordinates
	 */
	public static Vector2 worldToScreen(Vector2 world) {
		return new Vector2(worldToScreenX(world.x), worldToScreenY(world.y));
	}


	/**
	 * Converts world coordinates to screen coordinates
	 */
	public static float worldToScreenY(float worldY) {
		return getGraphics().getHeight()/2 + (worldY - getGraphics().getCam().position.y);
	}


	private static Graphics getGraphics() {
		return Wiring.injector().getInstance(Graphics.class);
	}
}