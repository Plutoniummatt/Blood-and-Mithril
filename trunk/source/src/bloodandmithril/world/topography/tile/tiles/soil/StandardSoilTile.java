package bloodandmithril.world.topography.tile.tiles.soil;

import bloodandmithril.world.topography.tile.tiles.SoilTile;

public class StandardSoilTile extends SoilTile {
	private static final long serialVersionUID = 8075638248883710200L;

	/**
	 * Constructor
	 */
	public StandardSoilTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 3;
	}


	@Override
	public void changeToStair() {
	}
}