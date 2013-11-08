package bloodandmithril.world.topography.tile.tiles.sedimentary;

import bloodandmithril.item.Item;
import bloodandmithril.item.material.mineral.YellowSand;
import bloodandmithril.world.topography.tile.tiles.SeditmentaryTile;

public class YellowSandTile extends SeditmentaryTile {
	private static final long serialVersionUID = 905567490661951934L;

	/**
	 * Constructor
	 */
	public YellowSandTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 5;
	}


	@Override
	public void changeToStair() {
	}


	@Override
	public void changeToSmoothCeiling() {
	}


	@Override
	public Item mine() {
		return new YellowSand();
	}
}