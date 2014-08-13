package bloodandmithril.world.topography.tile.tiles.sedimentary;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.mineral.earth.Sand;
import bloodandmithril.world.topography.tile.tiles.SeditmentaryTile;

@Copyright("Matthew Peck 2014")
public class SandTile extends SeditmentaryTile {
	private static final long serialVersionUID = 905567490661951934L;

	/**
	 * Constructor
	 */
	public SandTile() {
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
		return new Sand();
	}
}