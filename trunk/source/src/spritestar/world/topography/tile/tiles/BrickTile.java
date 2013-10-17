package spritestar.world.topography.tile.tiles;

import spritestar.world.topography.tile.Tile;

public abstract class BrickTile extends Tile {
	private static final long serialVersionUID = -2954379544199359506L;

	/**
	 * Constructor
	 */
	protected BrickTile(boolean isPlatformTile) {
		super(isPlatformTile);
	}
}
