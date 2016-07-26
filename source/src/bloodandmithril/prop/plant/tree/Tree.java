package bloodandmithril.prop.plant.tree;

import static bloodandmithril.graphics.Textures.GAME_WORLD_TEXTURE;
import static com.google.common.collect.Maps.newHashMap;

import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.graphics.RenderPropWith;
import bloodandmithril.graphics.Textures;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.plant.tree.alder.AlderTree;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.topography.tile.Tile;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
@UpdatedBy()
@RenderPropWith()
public abstract class Tree extends Prop {
	private static final long serialVersionUID = 4453602903027321858L;

	/** Trunk is made from {@link TreeSegment}s */
	private TreeSegment stump;
	
	/** The curvature of this {@link Tree} */
	public float rigidity = 1f;
	
	/** Used for curvature calculations */
	public float acceleration = 0f;
	public float velocity = 0f;
	public float curvature = 0f;
	
	public final float baseAngle = 0f;
	public final float maxThinningFactor;
	
	/**
	 * Constructor
	 */
	protected Tree(float x, float y, int width, int height, float maxThinningFactor, SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf) {
		super(x, y, width, height, true, Depth.MIDDLEGROUND, canPlaceOnTopOf, true);
		this.maxThinningFactor = maxThinningFactor;
	}
	
	
	public abstract int getTrunkOverlap();


	/**
	 * @return the height of the tree trunk
	 */
	public int getHeight() {
		return getStump().getTrunkHeight();
	}
	
	
	public static void setup() {
		HashMap<Integer, TextureRegion> testTreeTextures = newHashMap();
		testTreeTextures.put(0, new TextureRegion(GAME_WORLD_TEXTURE, 870, 132, 44, 51));
		testTreeTextures.put(1, new TextureRegion(GAME_WORLD_TEXTURE, 915, 132, 44, 51));
		testTreeTextures.put(2, new TextureRegion(GAME_WORLD_TEXTURE, 960, 132, 44, 51));
		testTreeTextures.put(3, new TextureRegion(GAME_WORLD_TEXTURE, 1005, 132, 44, 51));
		testTreeTextures.put(4, new TextureRegion(GAME_WORLD_TEXTURE, 1050, 132, 44, 51));
		testTreeTextures.put(5, new TextureRegion(GAME_WORLD_TEXTURE, 1095, 132, 44, 51));
		Textures.treeTextures.put(AlderTree.class, testTreeTextures);
	}


	public TreeSegment getStump() {
		return stump;
	}


	public void setStump(TreeSegment stump) {
		this.stump = stump;
	}
}