package spritestar.world.topography.tile.tiles;

import spritestar.world.topography.tile.Tile;

public abstract class SoilTile extends Tile {
	private static final long serialVersionUID = -538530432922549224L;

	/**
	 * Constructor
	 */
	protected SoilTile(boolean isPlatformTile) {
		super(isPlatformTile);
	}
}
