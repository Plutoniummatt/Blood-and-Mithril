package bloodandmithril.world.topography.tile.tiles.stone;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.tile.tiles.StoneTile;

@Copyright("Matthew Peck 2014")
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
	public void changeToSmoothCeiling() {
	}


	@Override
	public Item mine() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean isTransparent() {
		return false;
	}


	@Override
	public Color getMineExplosionColor() {
		return Color.DARK_GRAY;
	}


	@Override
	public int getSymmetryNumber() {
		return 1;
	}
}