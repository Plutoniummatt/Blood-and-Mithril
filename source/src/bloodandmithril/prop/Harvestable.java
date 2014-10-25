package bloodandmithril.prop;

import static bloodandmithril.world.Domain.Depth.FOREGOUND;

import java.util.Collection;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;

/**
 * Interface for harvesting
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Harvestable extends Prop {
	private static final long serialVersionUID = 2548436846590756693L;

	/**
	 * Constructor
	 */
	protected Harvestable(float x, float y, int width, int height, boolean grounded) {
		super(x, y, width, height, grounded, FOREGOUND);
	}

	/** Returns the item that harvesting this {@link Harvestable} provides */
	public abstract Collection<Item> harvest();

	/** True if the prop is destroyed upon being harvested */
	public abstract boolean destroyUponHarvest();
}