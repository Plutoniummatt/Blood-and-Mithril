package bloodandmithril.world.weather;

import bloodandmithril.core.Copyright;

/**
 * Represents a specific weather, i.e. rainy, sunny etc
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class Weather {

	/**
	 * Renders this weather
	 */
	public abstract void render();
}