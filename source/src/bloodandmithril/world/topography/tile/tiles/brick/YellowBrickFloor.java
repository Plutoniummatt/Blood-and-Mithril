package bloodandmithril.world.topography.tile.tiles.brick;

import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.tile.tiles.BrickTile;

public class YellowBrickFloor extends BrickTile {
	private static final long serialVersionUID = 2360428967780106537L;

	/**
	 * Constructor
	 */
	public YellowBrickFloor() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 12;
	}


	@Override
	public void changeToStair() {
	}


	@Override
	public void changeToSmoothCeiling() {
	}


	@Override
	public Item mine() {
		return null;
	}
}