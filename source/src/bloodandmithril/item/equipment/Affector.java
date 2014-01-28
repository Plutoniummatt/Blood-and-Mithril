package bloodandmithril.item.equipment;

import bloodandmithril.character.Individual;

/**
 * Affects another {@link Individual}
 *
 * @author Matt
 */
public interface Affector {
	public void affect(Individual victim);
}
