package bloodandmithril.world.topography.tile.tiles.stone;

import bloodandmithril.world.topography.tile.tiles.StoneTile;

public class GraniteTile extends StoneTile {
	private static final long serialVersionUID = -7556873953047888014L;

	/**
	 * Constructor
	 */
	public GraniteTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 4;
	}


	@Override
	public void changeToStair() {
	}


	@Override
	public void changeToSmoothCeiling() {
	}
}