package bloodandmithril.world.topography.tile.tiles.glass;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.GlassItem;
import bloodandmithril.world.topography.tile.tiles.GlassTile;

@Copyright("Matthew Peck 2014")
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
		return Color.WHITE;
	}


	@Override
	public int getSymmetryNumber() {
		return 1;
	}
}