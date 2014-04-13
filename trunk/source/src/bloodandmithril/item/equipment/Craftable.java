package bloodandmithril.item.equipment;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Item;
import bloodandmithril.prop.crafting.CraftingStation;

/**
 * Indicates something that can be crafted at a {@link CraftingStation}
 *
 * @author Matt
 */
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