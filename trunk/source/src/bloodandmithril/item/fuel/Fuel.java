package bloodandmithril.item.fuel;

import bloodandmithril.item.Item;
import bloodandmithril.prop.building.Furnace;


/**
 * Class that dictates whether an item can be used as a fuel. For combustion inside a {@link Furnace}
 *
 * @author Matt
 */
public interface Fuel {

	/** The duration which this {@link Fuel} will combust at a temperature of {@link Furnace#MIN_TEMP} */
	public float getCombustionDuration();

	/** What this {@link Fuel} will turn into when combusted */
	public Item consume();
}