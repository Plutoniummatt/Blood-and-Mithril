package bloodandmithril.item.items.misc;

import bloodandmithril.item.items.Item;

/**
 * Material
 *
 * @author Matt
 */
public abstract class Misc extends Item {

	private static final long serialVersionUID = -6616334367982345623L;

	/**
	 * Protected constructor
	 */
	protected Misc(float mass, boolean equippable, long value) {
		super(mass, equippable, value);
	}

	/**
	 * Protected constructor
	 */
	protected Misc(float mass, boolean equippable) {
		super(mass, equippable);
	}


	@Override
	public String getType() {
		return "Miscellaneous";
	}
}