package bloodandmithril.world.topography.tile.tiles.brick;

import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.construction.Brick;
import bloodandmithril.world.topography.tile.tiles.BrickTile;

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
		return new Brick();
	}
}