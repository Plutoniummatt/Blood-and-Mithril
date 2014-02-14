package bloodandmithril.item.material;

import bloodandmithril.prop.building.Furnace;


/**
 * Class that dictates whether an item can be used as a fuel. For combustion inside a {@link Furnace}
 *
 * @author Matt
 */
public interface Fuel {

	/** The duration which this {@link Fuel} will combust at a temperature of {@link Furnace#minTemp} */
	public float getCombustionDuration();
}