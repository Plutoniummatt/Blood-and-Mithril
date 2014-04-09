package bloodandmithril.item.equipment;

import bloodandmithril.character.Individual;
import bloodandmithril.prop.furniture.Anvil;

/**
 * Indicates something that can be smith'd at an {@link Anvil}
 *
 * @author Matt
 */
public interface Smithable {

	/**
	 * @return whether or not an {@link Individual} can smith this item.
	 */
	public default boolean canBeSmithedBy(Individual individual) {
		return individual.getSkills().getSmithing() >= getRequiredSmithingLevel();
	}

	/**
	 * @return the required smithing level to be able to smith this item.
	 */
	public int getRequiredSmithingLevel();
}