package bloodandmithril.item;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;

/**
 * Indicates something that can be crafted at a {@link CraftingStation}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface Craftable {

	/**
	 * @return whether or not an {@link Individual} can smith this item.
	 */
	public boolean canBeCraftedBy(Individual individual);

	/**
	 * @return the map of required materials to craft this {@link Craftable}
	 */
	public Map<Item, Integer> getRequiredMaterials();

	/**
	 * @return the time it will take to craft this item, in seconds.
	 */
	public float getCraftingDuration();
}