package bloodandmithril.world.topography.tile.tiles.stone;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.RockItem;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.world.topography.tile.tiles.StoneTile;

@Copyright("Matthew Peck 2014")
public class SandStoneTile extends StoneTile {
	private static final long serialVersionUID = -8637000501523026633L;
	private static Color mineExplosionColor = new Color(158f/255f, 136f/255f, 7f/255f, 1f);

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


	@Override
	public void changeToSmoothCeiling() {
	}


	@Override
	public Item mine() {
		return RockItem.rock(SandStone.class);
	}


	@Override
	public boolean isTransparent() {
		return false;
	}


	@Override
	public Color getMineExplosionColor() {
		return mineExplosionColor;
	}


	@Override
	public int getSymmetryNumber() {
		return 1;
	}
}