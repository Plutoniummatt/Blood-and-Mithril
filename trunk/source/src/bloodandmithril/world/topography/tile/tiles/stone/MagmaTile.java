package bloodandmithril.world.topography.tile.tiles.stone;

import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.tile.tiles.StoneTile;

public class MagmaTile extends StoneTile {
	private static final long serialVersionUID = -5132643453079579241L;

	/**
	 * Constructor
	 */
	public MagmaTile() {
		super(false);
	}
	

	@Override
	protected float getTexCoordYSpecific() {
		return 17;
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
