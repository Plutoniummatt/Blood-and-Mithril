package bloodandmithril.world.topography.tile.tiles.soil;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.mineral.earth.Dirt;
import bloodandmithril.world.topography.tile.tiles.SoilTile;

@Copyright("Matthew Peck 2014")
public class StandardSoilTile extends SoilTile {
	private static final long serialVersionUID = 8075638248883710200L;

	/**
	 * Constructor
	 */
	public StandardSoilTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 3;
	}


	@Override
	public void changeToStair() {
	}


	@Override
	public void changeToSmoothCeiling() {
	}


	@Override
	public Item mine() {
		return new Dirt();
	}
}