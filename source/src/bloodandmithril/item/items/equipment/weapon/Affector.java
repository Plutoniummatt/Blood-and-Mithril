package bloodandmithril.item.items.equipment.weapon;

import bloodandmithril.character.individuals.Individual;

/**
 * Affects another {@link Individual}
 *
 * @author Matt
 */
public interface Affector {

	/** Affected the victim */
	public void affect(Individual victim);
}
