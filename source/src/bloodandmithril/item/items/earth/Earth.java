package bloodandmithril.item.items.earth;

import bloodandmithril.item.items.Item;

/**
 * Earth/Dirt/Clay etc
 *
 * @author Matt
 */
public abstract class Earth extends Item {

	private static final long serialVersionUID = -6616334367982345623L;

	/**
	 * Protected constructor
	 */
	protected Earth(float mass, boolean equippable, long value) {
		super(mass, equippable, value);
	}


	@Override
	public String getType() {
		return "Earth";
	}
}