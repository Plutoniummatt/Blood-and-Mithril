package bloodandmithril.world.topography.tile.tiles.stone;

import bloodandmithril.world.topography.tile.tiles.StoneTile;

public class SandStoneTile extends StoneTile {
	private static final long serialVersionUID = -8637000501523026633L;

	/**
	 * Constructor
	 */
	public SandStoneTile() {
		super(false);
	}

	
	@Override
	protected float getTexCoordYSpecific() {
		return 6;
	}
}