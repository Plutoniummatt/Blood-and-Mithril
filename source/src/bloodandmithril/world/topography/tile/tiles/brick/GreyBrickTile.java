package bloodandmithril.world.topography.tile.tiles.brick;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Brick;
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
		return 13;
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
		return new Brick();
	}
}