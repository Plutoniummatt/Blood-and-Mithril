package bloodandmithril.world.topography.tile.tiles;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.topography.tile.Tile;

@Copyright("Matthew Peck 2014")
public abstract class SoilTile extends Tile {
	private static final long serialVersionUID = -538530432922549224L;

	/**
	 * Constructor
	 */
	protected SoilTile(boolean isPlatformTile) {
		super(isPlatformTile);
	}
}
