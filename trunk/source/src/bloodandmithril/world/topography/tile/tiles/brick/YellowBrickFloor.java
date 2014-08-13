package bloodandmithril.world.topography.tile.tiles.brick;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Brick;
import bloodandmithril.world.topography.tile.tiles.BrickTile;

@Copyright("Matthew Peck 2014")
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
		return new Brick();
	}
}