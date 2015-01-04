package bloodandmithril.world.topography.tile.tiles.stone;

import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.tile.tiles.StoneTile;

public class ObsidianTile extends StoneTile {
	private static final long serialVersionUID = -7817243590398073089L;

	/**
	 * Constructor
	 */
	public ObsidianTile() {
		super(false);
	}


	@Override
	protected float getTexCoordYSpecific() {
		return 14;
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
