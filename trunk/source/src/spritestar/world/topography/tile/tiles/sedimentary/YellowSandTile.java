package spritestar.world.topography.tile.tiles.sedimentary;

import spritestar.world.topography.tile.tiles.SeditmentaryTile;

public class YellowSandTile extends SeditmentaryTile {
	private static final long serialVersionUID = 905567490661951934L;

	/**
	 * Constructor
	 */
	public YellowSandTile() {
		super(false);
	}

	
	@Override
	protected float getTexCoordYSpecific() {
		return 5;
	}
}