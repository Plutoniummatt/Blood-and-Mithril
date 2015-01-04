package bloodandmithril.world.topography.tile.tiles.stone;

import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.tile.tiles.StoneTile;

public class LimeStoneTile extends StoneTile {
	private static final long serialVersionUID = -6334422937656140014L;

	/**
	 * Constructor
	 */
	public LimeStoneTile() {
		super(false);
	}
	

	@Override
	protected float getTexCoordYSpecific() {
		return 16;
	}
	

	@Override
	public void changeToStair() {
		
	}
	

	@Override
	public void changeToSmoothCeiling() {
		
	}
	

	@Override
	public Item mine() {
		// TODO Auto-generated method stub
		return null;
	}
}
