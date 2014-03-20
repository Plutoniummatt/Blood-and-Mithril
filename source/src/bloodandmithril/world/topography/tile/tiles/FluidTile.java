package bloodandmithril.world.topography.tile.tiles;

import bloodandmithril.item.Item;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * A special {@link Tile} that mimics (or attempts to) fluids.
 *
 * @author Matt
 */
public class FluidTile extends EmptyTile {
	private static final long serialVersionUID = -2071316573173011290L;

	/**
	 * The depth of this {@link FluidTile}
	 */
	private int depth;
	
	/**
	 * Constructor
	 */
	public FluidTile() {
		super();
	}

	
	@Override
	protected float getTexCoordYSpecific() {
		return 12;
	}

	
	@Override
	public void changeToStair() {
	}

	
	@Override
	public void changeToSmoothCeiling() {
	}

	
	@Override
	public Item mine() {
		return null;
	}


	public int getDepth() {
		return depth;
	}


	public void setDepth(int depth) {
		this.depth = depth;
	}
}