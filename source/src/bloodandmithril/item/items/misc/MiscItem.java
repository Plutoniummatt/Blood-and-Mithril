package bloodandmithril.item.items.misc;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;

/**
 * Material
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class MiscItem extends Item {

	private static final long serialVersionUID = -6616334367982345623L;

	/**
	 * Protected constructor
	 */
	protected MiscItem(float mass, int volume, boolean equippable, long value) {
		super(mass, volume, equippable, value);
	}

	/**
	 * Protected constructor
	 */
	protected MiscItem(float mass, int volume, boolean equippable) {
		super(mass, volume, equippable);
	}

	@Override
	public ItemCategory getType() {
		return ItemCategory.MISC;
	}

	@Override
	public float getUprightAngle() {
		return 90f;
	}
}