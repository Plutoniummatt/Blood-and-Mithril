package bloodandmithril.world.topography.tile.tiles.brick;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.BrickItem;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.world.topography.tile.tiles.BrickTile;

@Copyright("Matthew Peck 2014")
public class GreyBrickTile extends BrickTile {
	private static final long serialVersionUID = 7288704521958945616L;

	/**
	 * Constructor
	 */
	public GreyBrickTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return smoothCeiling ? 14 : 13;
	}


	@Override
	public void changeToSmoothCeiling() {
		this.smoothCeiling = true;
	}


	@Override
	public Item mine() {
		return BrickItem.brick(SandStone.class);
	}


	@Override
	public boolean isTransparent() {
		return false;
	}


	@Override
	public Color getMineExplosionColor() {
		return Color.DARK_GRAY;
	}
}