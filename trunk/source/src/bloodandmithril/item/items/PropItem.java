package bloodandmithril.item.items;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class PropItem extends Item {
	private static final long serialVersionUID = -7060370863759778105L;

	/**
	 * Constructor
	 */
	protected PropItem(float mass, int volume, boolean equippable, long value) {
		super(mass, volume, equippable, value);
	}

	public abstract Prop getProp();

	@Override
	public float getUprightAngle() {
		return 90f;
	}
}