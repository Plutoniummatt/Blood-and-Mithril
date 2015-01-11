package bloodandmithril.world.topography.tile.tiles.stone;

import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.tile.tiles.StoneTile;


public class CrystalTile extends StoneTile {
	private static final long serialVersionUID = 8040892779369761098L;

	
	/**
	 * Constructor
	 */
	public CrystalTile() {
		super(false);
	}

	
	@Override
	protected float getTexCoordYSpecific() {
		return 18;
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
