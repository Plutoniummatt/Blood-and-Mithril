package bloodandmithril.prop;

import java.util.Collection;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;

/**
 * Interface for harvesting
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface Harvestable {

	/** Returns the item that harvesting this {@link Harvestable} provides */
	public abstract Collection<Item> harvest(boolean canReceive);

	/** True if the prop is destroyed upon being harvested */
	public abstract boolean destroyUponHarvest();
}