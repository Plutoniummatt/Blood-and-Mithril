package bloodandmithril.item.items.mineral;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;

/**
 * Minerals are naturally occuring things like rocks, crystals, dirt etc.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class MineralItem extends Item {
	private static final long serialVersionUID = -8653778217712948229L;

	/**
	 * Protected constructor
	 */
	protected MineralItem(float mass, int volume, boolean equippable, long value) {
		super(mass, volume, equippable, value);
	}

	@Override
	public float getUprightAngle() {
		return 90f;
	}
}
