package bloodandmithril.world.topography.tile.tiles.brick;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Brick;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.world.topography.tile.tiles.BrickTile;

@Copyright("Matthew Peck 2014")
public class YellowBrickTile extends BrickTile {
	private static final long serialVersionUID = 8759613388552944453L;

	/**
	 * Constructor
	 */
	public YellowBrickTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 8;
	}


	@Override
	public void changeToStair() {
		this.isStair = true;
	}


	@Override
	public void changeToSmoothCeiling() {
		this.smoothCeiling = true;
	}


	@Override
	public Item mine() {
		return Brick.brick(SandStone.class);
	}


	@Override
	public boolean isTransparent() {
		return false;
	}
}