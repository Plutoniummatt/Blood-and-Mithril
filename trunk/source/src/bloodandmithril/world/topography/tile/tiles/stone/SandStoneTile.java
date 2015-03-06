package bloodandmithril.world.topography.tile.tiles.stone;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Rock;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.world.topography.tile.tiles.StoneTile;

@Copyright("Matthew Peck 2014")
public class SandStoneTile extends StoneTile {
	private static final long serialVersionUID = -8637000501523026633L;

	/**
	 * Constructor
	 */
	public SandStoneTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 6;
	}


	@Override
	public void changeToStair() {
	}


	@Override
	public void changeToSmoothCeiling() {
	}


	@Override
	public Item mine() {
		return Rock.rock(SandStone.class);
	}


	@Override
	public boolean isTransparent() {
		return false;
	}
}