package bloodandmithril.item.items.mineral.earth;

import bloodandmithril.item.items.mineral.MineralItem;


/**
 * Earth/Dirt/Clay etc
 *
 * @author Matt
 */
public abstract class EarthItem extends MineralItem {

	private static final long serialVersionUID = -6616334367982345623L;

	/**
	 * Protected constructor
	 */
	protected EarthItem(float mass, int volume, boolean equippable, long value) {
		super(mass, volume, equippable, value);
	}


	@Override
	public Category getType() {
		return Category.EARTH;
	}
}