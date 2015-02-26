package bloodandmithril.item;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.construction.craftingstation.FueledCraftingStation;

/**
 * Class that dictates whether an item can be used as a fuel. For combustion inside a {@link FueledCraftingStation}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface Fuel {

	/** The duration which this {@link Fuel} will combust */
	public float getCombustionDuration();

	/** What this {@link Fuel} will turn into when combusted */
	public Item consume();
}