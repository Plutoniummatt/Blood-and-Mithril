package bloodandmithril.control;

import bloodandmithril.core.Copyright;

/**
 * Handles a left click, specific to a functional area of the game
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface LeftClickHandler {

	/**
	 * Handles the left click, returning true if subsequent handlers should be ignored
	 */
	public boolean leftClick();
}