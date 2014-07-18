package bloodandmithril.world.topography.tile.tiles.brick;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.tile.tiles.BrickTile;

@Copyright("Matthew Peck 2014")
public class YellowBrickPlatform extends BrickTile {
	private static final long serialVersionUID = 2360428967780106537L;

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