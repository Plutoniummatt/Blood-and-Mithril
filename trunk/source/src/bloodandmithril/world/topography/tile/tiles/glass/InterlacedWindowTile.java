package bloodandmithril.world.topography.tile.tiles.glass;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.GlassItem;
import bloodandmithril.world.topography.tile.tiles.GlassTile;

@Copyright("Matthew Peck 2014")
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
		return new GlassItem();
	}


	@Override
	public boolean isTransparent() {
		return true;
	}


	@Override
	public Color getMineExplosionColor() {
		return Color.DARK_GRAY;
	}
}