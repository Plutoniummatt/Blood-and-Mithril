package bloodandmithril.world.topography.tile.tiles.glass;

import bloodandmithril.item.Item;
import bloodandmithril.world.topography.tile.tiles.GlassTile;

public class InterlacedWindowTile extends GlassTile {
	private static final long serialVersionUID = -896847725021646085L;

	/**
	 * Constructor
	 */
	public InterlacedWindowTile() {
		super(false);
	}
	

	@Override
	protected float getTexCoordYSpecific() {
		return 11;
	}

	
	@Override
	public void changeToStair() {
	}

	
	@Override
	public void changeToSmoothCeiling() {
	}
	

	@Override
	public Item mine() {
		// TODO Auto-generated method stub
		return null;
	}
}