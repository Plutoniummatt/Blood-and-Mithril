package bloodandmithril.world.topography.tile.tiles.soil;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.plant.Seed;
import bloodandmithril.item.items.mineral.earth.Dirt;
import bloodandmithril.world.topography.tile.tiles.SoilTile;

@Copyright("Matthew Peck 2014")
public class DryDirtTile extends SoilTile {
	private static final long serialVersionUID = 8728088158152709991L;

	/**
	 * Constructor
	 */
	public DryDirtTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 7;
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


	@Override
	public boolean canPlant(Seed seed) {
		return false;
	}


	@Override
	public boolean isTransparent() {
		return false;
	}
}