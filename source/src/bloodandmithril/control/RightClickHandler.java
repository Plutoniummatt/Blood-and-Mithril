package bloodandmithril.control;

import bloodandmithril.core.Copyright;

/**
 * Handles a right click, specific to a functional area of the game
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface RightClickHandler {

	/**
	 * Handles the right click, returning true if subsequent handlers should be ignored
	 */
	public boolean rightClick(boolean doubleClick);
}