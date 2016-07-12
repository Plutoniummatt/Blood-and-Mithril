package bloodandmithril.control;

import bloodandmithril.core.Copyright;

/**
 * Handles input, specific to a functional area of the game
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface KeyPressedHandler {

	/**
	 * Handles the key press, returning true if subsequent handlers should be ignored
	 */
	public boolean handle(int keycode);
}