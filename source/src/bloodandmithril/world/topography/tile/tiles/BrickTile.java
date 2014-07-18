package bloodandmithril.world.topography.tile.tiles;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.topography.tile.Tile;

@Copyright("Matthew Peck 2014")
public abstract class BrickTile extends Tile {
	private static final long serialVersionUID = -2954379544199359506L;

	/**
	 * Constructor
	 */
	protected BrickTile(boolean isPlatformTile) {
		super(isPlatformTile);
	}
}
