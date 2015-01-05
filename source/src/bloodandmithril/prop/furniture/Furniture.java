package bloodandmithril.prop.furniture;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.Domain.Depth;
import bloodandmithril.world.topography.tile.Tile;

@Copyright("Matthew Peck 2014")
public abstract class Furniture extends Prop {
	private static final long serialVersionUID = -1643197661469081725L;

	/**
	 * Protected constructor
	 */
	protected Furniture(float x, float y, int width, int height, boolean grounded) {
		super(x, y, width, height, grounded, Depth.MIDDLEGROUND, new NonPassableTilesOnly());
	}


	public static class NonPassableTilesOnly extends SerializableMappingFunction<Tile, Boolean> {
		private static final long serialVersionUID = 3610496009117486193L;

		@Override
		public Boolean apply(Tile input) {
			return !input.isPassable();
		}
	}
}