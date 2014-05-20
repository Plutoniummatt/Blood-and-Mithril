package bloodandmithril.item.plant;

import bloodandmithril.item.Item;

/**
 * A seed which grows into a plant
 *
 * @author Matt
 */
public abstract class Seed extends Item {
	private static final long serialVersionUID = -6248381190070613796L;

	/**
	 * Constructor
	 */
	protected Seed(float mass, boolean equippable, long value) {
		super(mass, equippable, value);
	}
}