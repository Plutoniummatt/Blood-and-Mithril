package bloodandmithril.world.topography.tile.tiles;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.topography.tile.Tile;

@Copyright("Matthew Peck 2014")
public abstract class GlassTile extends Tile {
	private static final long serialVersionUID = -466633989013346028L;

	/**
	 * Constructor
	 */
	protected GlassTile(boolean isPlatformTile) {
		super(isPlatformTile);
	}
}