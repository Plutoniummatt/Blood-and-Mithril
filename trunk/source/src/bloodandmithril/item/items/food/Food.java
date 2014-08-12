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
	protected Food(float mass, boolean equippable, long value) {
		super(mass, equippable, value);
	}


	@Override
	public String getType() {
		return "Food";
	}
}