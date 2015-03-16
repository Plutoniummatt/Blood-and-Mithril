package bloodandmithril.item.items.furniture;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.PropItem;

/**
 * Material
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class FurnitureItem extends PropItem {

	private static final long serialVersionUID = -6616334367982345623L;

	/**
	 * Protected constructor
	 */
	protected FurnitureItem(float mass, int volume, long value) {
		super(mass, volume, false, value);
	}


	@Override
	public Category getType() {
		return Category.FURNITURE;
	}
}