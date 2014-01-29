package bloodandmithril.item.equipment;

import bloodandmithril.character.Individual;

/**
 * Affects another {@link Individual}
 *
 * @author Matt
 */
public interface Affector {

	/** Affected the victim */
	public void affect(Individual victim);
}
