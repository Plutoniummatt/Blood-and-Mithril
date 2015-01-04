package bloodandmithril.world.topography.tile.tiles.sedimentary;

import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.tile.tiles.SeditmentaryTile;

public class RedSandTile extends SeditmentaryTile {
	private static final long serialVersionUID = 7634028791495864825L;

	/**
	 * Constructor
	 */
	public RedSandTile() {
		super(false);
	}
	

	@Override
	protected float getTexCoordYSpecific() {
		return 15;
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
