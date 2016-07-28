package bloodandmithril.world.topography.tile.tiles.brick;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.BrickItem;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.world.topography.tile.tiles.BrickTile;

@Copyright("Matthew Peck 2014")
public class YellowBrickPlatform extends BrickTile {
	private static final long serialVersionUID = 2360428967780106537L;
	private static Color mineExplosionColor = new Color(158f/255f, 136f/255f, 7f/255f, 1f);

	/**
	 * Constructor
	 */
	public YellowBrickPlatform() {
		super(true);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 10;
	}


	@Override
	public void changeToSmoothCeiling() {
	}


	@Override
	public Item mine() {
		return BrickItem.brick(SandStone.class);
	}


	@Override
	public boolean isTransparent() {
		return true;
	}


	@Override
	public Color getMineExplosionColor() {
		return mineExplosionColor;
	}
}