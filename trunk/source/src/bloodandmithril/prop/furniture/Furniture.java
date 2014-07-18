package bloodandmithril.prop.furniture;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain.Depth;

@Copyright("Matthew Peck 2014")
public abstract class Furniture extends Prop {
	private static final long serialVersionUID = -1643197661469081725L;

	/**
	 * Protected constructor
	 */
	protected Furniture(float x, float y, int width, int height, boolean grounded, boolean snapToGrid) {
		super(x, y, width, height, grounded, Depth.MIDDLEGROUND);
	}
}