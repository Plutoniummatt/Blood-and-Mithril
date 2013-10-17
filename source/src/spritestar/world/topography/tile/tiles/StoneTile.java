package spritestar.world.topography.tile.tiles;

import spritestar.world.topography.tile.Tile;

public abstract class StoneTile extends Tile {
	private static final long serialVersionUID = 1007359407760447310L;

	/**
	 * Constructor
	 */
	protected StoneTile(boolean isPlatformTile) {
		super(isPlatformTile);
	}
}