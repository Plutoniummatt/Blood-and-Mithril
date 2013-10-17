package spritestar.world.topography.tile.tiles.brick;

import spritestar.world.topography.tile.tiles.BrickTile;

public class YellowBrickPlatform extends BrickTile {
	private static final long serialVersionUID = 2360428967780106537L;

	/**
	 * Constructor
	 */
	public YellowBrickPlatform() {
		super(true);
	}

	
	@Override
	protected float getTexCoordYSpecific() {
		return 10;
	}
}