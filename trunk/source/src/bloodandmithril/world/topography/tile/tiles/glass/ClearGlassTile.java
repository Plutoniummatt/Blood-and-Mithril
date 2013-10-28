package bloodandmithril.world.topography.tile.tiles.glass;

import bloodandmithril.world.topography.tile.tiles.GlassTile;

public class ClearGlassTile extends GlassTile {
	private static final long serialVersionUID = -6196904136714852677L;

	/**
	 * Constructor
	 */
	public ClearGlassTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 9;
	}


	@Override
	public void changeToStair() {
	}


	@Override
	public void changeToSmoothCeiling() {
	}
}