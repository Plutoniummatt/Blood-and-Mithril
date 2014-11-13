package bloodandmithril.item.items.material;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;

/**
 * Material
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Material extends Item {

	private static final long serialVersionUID = -6616334367982345623L;

	/**
	 * Protected constructor
	 */
	protected Material(float mass, int volume, boolean equippable, long value) {
		super(mass, volume, equippable, value);
	}

	/**
	 * Protected constructor
	 */
	protected Material(float mass, int volume, boolean equippable) {
		super(mass, volume, equippable);
	}


	@Override
	public Category getType() {
		return Category.MATERIAL;
	}
}