package bloodandmithril.generation.component.components;

import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.tools.SawToothGenerator;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A square with jagged edges.
 *
 * @author Sam
 */
public class JaggedSquare extends Component{
	private static final long serialVersionUID = 8493868343330973687L;

	protected ConcurrentHashMap<Integer, Integer> top = new ConcurrentHashMap<Integer, Integer>();
	protected ConcurrentHashMap<Integer, Integer> bottom = new ConcurrentHashMap<Integer, Integer>();
	protected ConcurrentHashMap<Integer, Integer> left = new ConcurrentHashMap<Integer, Integer>();
	protected ConcurrentHashMap<Integer, Integer> right = new ConcurrentHashMap<Integer, Integer>();

	protected int tLineVariation;

	protected Class<? extends Tile> innerTileType;

	protected Class<? extends Tile> outerTileType;




	/**
	 * @param tLineVariation - How far the line of the square can vary inward from the boundaries.
	 * @param innerTileType - The {@link Tile} used within the shape.
	 * @param outerTileType - The {@link Tile} used around the shape.
	 */
	protected JaggedSquare(Boundaries boundaries, int structureKey, int tLineVariation, Class<? extends Tile> innerTileType, Class<? extends Tile> outerTileType) {
		super(boundaries, structureKey);
		this.tLineVariation = tLineVariation;
		this.innerTileType = innerTileType;
		this.outerTileType = outerTileType;

		SawToothGenerator top = new SawToothGenerator(boundaries.top - tLineVariation, boundaries.top, 1, 2, 30);
		SawToothGenerator bottom = new SawToothGenerator(boundaries.bottom, boundaries.bottom + tLineVariation, 1, 2, 30);
		SawToothGenerator left = new SawToothGenerator(boundaries.left, boundaries.left + tLineVariation, 1, 2, 30);
		SawToothGenerator right = new SawToothGenerator(boundaries.right - tLineVariation, boundaries.right, 1, 2, 30);

		for(int x = boundaries.left - 40; x < boundaries.right + 40; x++) {
			top.generateSurfaceHeight(x, true, this.top);
			bottom.generateSurfaceHeight(x, true, this.bottom);
		}
		for(int y = boundaries.bottom - 40; y < boundaries.top + 40; y++) {
			left.generateSurfaceHeight(y, true, this.left);
			right.generateSurfaceHeight(y, true, this.right);
		}
	}


	@Override
	public Tile getForegroundTile(int worldTileX, int worldTileY) {
		if(left.get(worldTileY) != null && worldTileX > left.get(worldTileY) && right.get(worldTileY) != null && worldTileX < right.get(worldTileY) && bottom.get(worldTileX) != null && worldTileY > bottom.get(worldTileX) && top.get(worldTileX) != null && worldTileY < top.get(worldTileX)) {
			try {
				return innerTileType == null ? null : innerTileType.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			try {
				return outerTileType == null ? null : outerTileType.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		if(left.get(worldTileY) != null && worldTileX > left.get(worldTileY) && right.get(worldTileY) != null && worldTileX < right.get(worldTileY) && bottom.get(worldTileX) != null && worldTileY > bottom.get(worldTileX) && top.get(worldTileX) != null && worldTileY < top.get(worldTileX)) {
			try {
				return innerTileType == null ? null : innerTileType.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			try {
				return outerTileType == null ? null : outerTileType.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	protected void generateInterfaces() {
		//Not needed, stemming will not be used.
	}

	@Override
	protected <T extends Component> Component internalStem(Class<T> with, ComponentCreationCustomization<T> custom) {
		//Not needed, stemming will not be used.
		return null;
	}
}
