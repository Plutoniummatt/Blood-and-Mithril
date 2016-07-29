package bloodandmithril.world.topography.tile.tiles.brick;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.BrickItem;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.world.topography.tile.tiles.BrickTile;

@Copyright("Matthew Peck 2014")
public class YellowBrickTile extends BrickTile {
	private static final long serialVersionUID = 8759613388552944453L;
	private static Color mineExplosionColor = new Color(158f/255f, 136f/255f, 7f/255f, 1f);

	/**
	 * Constructor
	 */
	public YellowBrickTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return smoothCeiling ? 9 : 8;
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
		return mineExplosionColor;
	}
}