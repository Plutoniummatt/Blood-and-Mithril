package bloodandmithril.item.material.plant;

import bloodandmithril.item.Item;

/**
 * A seed which grows into a plant
 *
 * @author Matt
 */
public abstract class Seed extends Item {

	/**
	 * Constructor
	 */
	protected Seed(float mass, boolean equippable, long value) {
		super(mass, equippable, value);
	}
}