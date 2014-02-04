package bloodandmithril.prop;

import bloodandmithril.item.Item;

/**
 * Interface for harvesting
 *
 * @author Matt
 */
public abstract class Harvestable extends Prop {

	/**
	 * Constructor
	 */
	protected Harvestable(float x, float y, boolean grounded) {
		super(x, y, grounded, false);
	}

	/** Returns the item that harvesting this {@link Harvestable} provides */
	public abstract Item harvest();

	/** True if the prop is destroyed upon being harvested */
	public abstract boolean destroyUponHarvest();
}