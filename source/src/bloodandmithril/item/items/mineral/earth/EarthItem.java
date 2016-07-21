package bloodandmithril.item.items.mineral.earth;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.mineral.MineralItem;


/**
 * Earth/Dirt/Clay etc
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public abstract class EarthItem extends MineralItem {

	private static final long serialVersionUID = -6616334367982345623L;

	/**
	 * Protected constructor
	 */
	protected EarthItem(final float mass, final int volume, final boolean equippable, final long value) {
		super(mass, volume, equippable, value);
	}


	@Override
	public ItemCategory getType() {
		return ItemCategory.EARTH;
	}
}