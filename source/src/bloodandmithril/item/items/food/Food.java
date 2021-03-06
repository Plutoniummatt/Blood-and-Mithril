package bloodandmithril.item.items.food;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.items.Item;

/**
 * Food
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Food extends Item implements Consumable {
	private static final long serialVersionUID = -6616334367982345623L;

	/**
	 * Protected constructor
	 */
	protected Food(float mass, int volume, boolean equippable, long value) {
		super(mass, volume, equippable, value);
	}


	@Override
	public ItemCategory getType() {
		return ItemCategory.FOOD;
	}

	@Override
	public float getUprightAngle() {
		return 90f;
	}
}