package bloodandmithril.generation.component.components;

import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.tools.SawToothGenerator;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;

public class Cavern extends Component{
	private static final long serialVersionUID = 3127757158880718928L;
	
	private Class<? extends Tile> tileType;
	
	protected ConcurrentHashMap<Integer, Integer> outerTop = new ConcurrentHashMap<Integer, Integer>();
	protected ConcurrentHashMap<Integer, Integer> outerBottom = new ConcurrentHashMap<Integer, Integer>();
	protected ConcurrentHashMap<Integer, Integer> outerLeft = new ConcurrentHashMap<Integer, Integer>();
	protected ConcurrentHashMap<Integer, Integer> outerRight = new ConcurrentHashMap<Integer, Integer>();
	protected ConcurrentHashMap<Integer, Integer> innerTop = new ConcurrentHashMap<Integer, Integer>();
	protected ConcurrentHashMap<Integer, Integer> innerBottom = new ConcurrentHashMap<Integer, Integer>();
	protected ConcurrentHashMap<Integer, Integer> innerLeft = new ConcurrentHashMap<Integer, Integer>();
	protected ConcurrentHashMap<Integer, Integer> innerRight = new ConcurrentHashMap<Integer, Integer>();

	
	/**
	 * Constructor
	 */
	public Cavern(Boundaries outerBoundaries, int structureKey, int outerBoundaryVariation, Boundaries innerBoundaries, int wallVariation, int floorVariation, int ceilingVariation, Class<? extends Tile> tileType) {
		super(outerBoundaries, structureKey);
		this.tileType = tileType;
		
		SawToothGenerator outerTop = new SawToothGenerator(outerBoundaries.top - outerBoundaryVariation, outerBoundaries.top, 1, 2, 30);
		SawToothGenerator outerBottom = new SawToothGenerator(outerBoundaries.bottom, outerBoundaries.bottom + outerBoundaryVariation, 1, 2, 30);
		SawToothGenerator outerLeft = new SawToothGenerator(outerBoundaries.left, outerBoundaries.left + outerBoundaryVariation, 1, 2, 30);
		SawToothGenerator outerRight = new SawToothGenerator(outerBoundaries.right - outerBoundaryVariation, outerBoundaries.right, 1, 2, 30);
		
		SawToothGenerator innerTop = new SawToothGenerator(innerBoundaries.top, innerBoundaries.top + ceilingVariation, 1, 2, 30);
		SawToothGenerator innerBottom = new SawToothGenerator(innerBoundaries.bottom - floorVariation, innerBoundaries.bottom, 1, 2, 30);
		SawToothGenerator innerLeft = new SawToothGenerator(innerBoundaries.left - wallVariation, innerBoundaries.left, 1, 2, 30);
		SawToothGenerator innerRight = new SawToothGenerator(innerBoundaries.right, innerBoundaries.right + wallVariation, 1, 2, 30);
		
		for(int x = outerBoundaries.left; x < outerBoundaries.right; x++) {
			outerTop.generateSurfaceHeight(x, true, this.outerTop);
			outerBottom.generateSurfaceHeight(x, true, this.outerBottom);
		}
		for(int y = outerBoundaries.bottom; y < outerBoundaries.top; y++) {
			outerLeft.generateSurfaceHeight(y, true, this.outerLeft);
			outerRight.generateSurfaceHeight(y, true, this.outerRight);
		}
		for(int x = innerBoundaries.left - wallVariation; x < innerBoundaries.right + wallVariation; x++) {
			innerTop.generateSurfaceHeight(x, true, this.innerTop);
			innerBottom.generateSurfaceHeight(x, true, this.innerBottom);
		}
		for(int y = innerBoundaries.bottom - floorVariation; y < innerBoundaries.top + ceilingVariation; y++) {
			innerLeft.generateSurfaceHeight(y, true, this.innerLeft);
			innerRight.generateSurfaceHeight(y, true, this.innerRight);
		}
	}

	
	@Override
	public Tile getForegroundTile(int worldTileX, int worldTileY) {
		if (
			outerTop.get(worldTileX) != null && worldTileY < outerTop.get(worldTileX) &&
			outerBottom.get(worldTileX) != null && worldTileY > outerBottom.get(worldTileX) &&
			outerLeft.get(worldTileY) != null && worldTileX > outerLeft.get(worldTileY) &&
			outerRight.get(worldTileY) != null && worldTileX < outerRight.get(worldTileY)
		) {
			if(
			innerTop.get(worldTileX) != null && worldTileY < innerTop.get(worldTileX) &&
			innerBottom.get(worldTileX) != null && worldTileY > innerBottom.get(worldTileX) &&
			innerLeft.get(worldTileY) != null && worldTileX > innerLeft.get(worldTileY) &&
			innerRight.get(worldTileY) != null && worldTileX < innerRight.get(worldTileY)
			) {
				return new Tile.EmptyTile();
			} else {
				try {
					return tileType == null ? null : tileType.newInstance();
				} catch (Exception e) {
					throw new RuntimeException();
				}
			}
		} else {
			return null;
		}
	}
	

	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		if (
				outerTop.get(worldTileX) != null && worldTileY < outerTop.get(worldTileX) &&
				outerBottom.get(worldTileX) != null && worldTileY > outerBottom.get(worldTileX) &&
				outerLeft.get(worldTileY) != null && worldTileX > outerLeft.get(worldTileY) &&
				outerRight.get(worldTileY) != null && worldTileX < outerRight.get(worldTileY)
		) {
			try {
				return tileType == null ? null : tileType.newInstance();
			} catch (Exception e) {
				throw new RuntimeException();
			}
		} else {
			return null;
		}
	}
	

	@Override
	protected void generateInterfaces() {
		
	}
	

	@Override
	protected <T extends Component> Component internalStem(Class<T> with, ComponentCreationCustomization<T> custom) {
		return null;
	}
}
