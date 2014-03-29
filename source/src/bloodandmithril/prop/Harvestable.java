package bloodandmithril.prop;

import static bloodandmithril.world.Domain.Depth.FOREGOUND;
import bloodandmithril.item.Item;

/**
 * Interface for harvesting
 *
 * @author Matt
 */
public abstract class Harvestable extends Prop {
	private static final long serialVersionUID = 2548436846590756693L;

	/**
	 * Constructor
	 */
	protected Harvestable(float x, float y, boolean grounded) {
		super(x, y, grounded, FOREGOUND);
	}
	
	@Override
	public String getContextMenuItemLabel() {
		return getClass().getSimpleName();
	}

	/** Returns the item that harvesting this {@link Harvestable} provides */
	public abstract Item harvest();

	/** True if the prop is destroyed upon being harvested */
	public abstract boolean destroyUponHarvest();
}