package bloodandmithril.world.topography.tile.tiles.soil;

import bloodandmithril.world.topography.tile.tiles.SoilTile;

public class DryDirtTile extends SoilTile {
	private static final long serialVersionUID = 8728088158152709991L;

	/**
	 * Constructor
	 */
	public DryDirtTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 7;
	}


	@Override
	public void changeToStair() {
	}


	@Override
	public void changeToSmoothCeiling() {
	}
}