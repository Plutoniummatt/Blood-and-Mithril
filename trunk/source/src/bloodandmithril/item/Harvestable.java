package bloodandmithril.item;

import bloodandmithril.prop.Prop;

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
}