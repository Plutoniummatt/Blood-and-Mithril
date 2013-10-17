package spritestar.world.generation.superstructures;

import static spritestar.world.generation.settings.GlobalGenerationSettings.*;

import spritestar.util.datastructure.Boundaries;
import spritestar.world.generation.StructureMap;
import spritestar.world.generation.SuperStructure;
import spritestar.world.generation.patterns.Layers;
import spritestar.world.generation.tools.SawToothGenerator;
import spritestar.world.generation.tools.RectangularSpaceCalculator;
import spritestar.world.topography.Topography;
import spritestar.world.topography.tile.Tile;


/**
 * The structure of a hill surface to be stored and used to generate when needed.
 * 
 * @author Sam, Matt
 */
public class Hills extends SuperStructure {
	private static final long serialVersionUID = -2162566918693231667L;

	@Override
	protected Boundaries findSpace(int startingChunkX, int startingChunkY) {
		//calculates where the structure can go
		return RectangularSpaceCalculator.calculateBoundaries(
			true,
			startingChunkX,
			startingChunkY,
			(hillsMaxWidth - hillsMinWidth) / 2 + 1,
			maxSurfaceHeight - (hillsMinHeight / Topography.chunkSize) + 1,
			maxSurfaceHeight,
			hillsMinHeight / Topography.chunkSize - 1
		);
	}


	@Override
	protected boolean isValid() {
		//if the structure space is out of bounds, don't make one
		return !(boundaries.bottom < hillsMinHeight/Topography.chunkSize - 1 || boundaries.top > maxSurfaceHeight);
	}


	@Override
	protected void generateStructure(boolean generatingToRight) {
		SawToothGenerator surfaceGenerator = new SawToothGenerator(hillsMinHeight, hillsMaxHeight, 0, 0, 30);
		
		// generate the surface height across the structure.
		int rightMostTile = (boundaries.right + 1) * Topography.chunkSize - 1;
		int leftMostTile = boundaries.left * Topography.chunkSize;
		if (generatingToRight) {
			for (int x = leftMostTile; x <= rightMostTile; x++) {
				if (x == rightMostTile - 40) {
					surfaceGenerator.setMaxSurface(hillsMinHeight);
				}
				surfaceGenerator.generateSurfaceHeight(x, generatingToRight, StructureMap.surfaceHeight);
			}
		} else {
			for (int x = rightMostTile; x >= leftMostTile; x--) {
				if (x == leftMostTile + 40) {
					surfaceGenerator.setMaxSurface(hillsMinHeight);
				}
				surfaceGenerator.generateSurfaceHeight(x, generatingToRight, StructureMap.surfaceHeight);
			}
		}		
	}
	
	
	@Override
	public Tile getForegroundTile(int worldTileX, int worldTileY) {
		if (worldTileY > StructureMap.surfaceHeight.get(worldTileX)) {
			return new Tile.EmptyTile();
		} else {
			return Layers.getTile(worldTileX, worldTileY);
		}
	}


	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		if (worldTileY + 2 > StructureMap.surfaceHeight.get(worldTileX)) {
			return new Tile.EmptyTile();
		} else {
			return Layers.getTile(worldTileX, worldTileY);
		}
	}


	@Override
	protected void generateSubStructures(boolean generatingToRight) {
		//TODO generateSubStructures
	}
}