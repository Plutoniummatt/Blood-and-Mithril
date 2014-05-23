package bloodandmithril.item;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.items.Item;

/**
 * Implement this with an {@link Item} to make it Consumable
 *
 * @author Matt
 */
public interface Consumable {

	/** Affect the {@link Individual} that has consumed this {@link Consumable} */
	public boolean consume(Individual consumer);
}