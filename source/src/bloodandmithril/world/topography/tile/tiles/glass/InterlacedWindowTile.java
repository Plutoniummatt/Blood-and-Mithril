package bloodandmithril.world.topography.tile.tiles.glass;

import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Glass;
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
		return new Glass();
	}
}