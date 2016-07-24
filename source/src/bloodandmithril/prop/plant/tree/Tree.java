package bloodandmithril.prop.plant.tree;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.topography.tile.Tile;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
@UpdatedBy()
public abstract class Tree extends Prop {
	private static final long serialVersionUID = 4453602903027321858L;

	/** Trunk is made from {@link TreeSegment}s */
	protected TreeSegment stump;
	
	/**
	 * Constructor
	 */
	protected Tree(float x, float y, int width, int height, SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf) {
		super(x, y, width, height, true, Depth.MIDDLEGROUND, canPlaceOnTopOf, true);
	}


	/**
	 * @return the height of the tree trunk
	 */
	public int getHeight() {
		return stump.getTrunkHeight();
	}

	
	/**
	 * Sets up textures for this tree, only called client-side
	 */
	public abstract void setupTextures();
}