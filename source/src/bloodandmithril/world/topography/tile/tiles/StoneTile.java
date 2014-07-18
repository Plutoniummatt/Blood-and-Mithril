package bloodandmithril.world.topography.tile.tiles;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.topography.tile.Tile;

@Copyright("Matthew Peck 2014")
public abstract class StoneTile extends Tile {
	private static final long serialVersionUID = 1007359407760447310L;

	/**
	 * Constructor
	 */
	protected StoneTile(boolean isPlatformTile) {
		super(isPlatformTile);
	}
}