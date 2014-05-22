package bloodandmithril.item.items.material;

import bloodandmithril.item.items.Item;

/**
 * Material
 *
 * @author Matt
 */
public abstract class Material extends Item {

	private static final long serialVersionUID = -6616334367982345623L;

	/**
	 * Protected constructor
	 */
	protected Material(float mass, boolean equippable, long value) {
		super(mass, equippable, value);
	}

	/**
	 * Protected constructor
	 */
	protected Material(float mass, boolean equippable) {
		super(mass, equippable);
	}


	@Override
	public String getType() {
		return "Material";
	}
}