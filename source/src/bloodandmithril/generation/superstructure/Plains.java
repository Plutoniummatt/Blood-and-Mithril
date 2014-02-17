package bloodandmithril.generation.superstructure;

import static bloodandmithril.generation.settings.GlobalGenerationSettings.maxSurfaceHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.plainsMaxHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.plainsMaxWidth;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.plainsMinHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.plainsMinWidth;

import bloodandmithril.generation.StructureMap;
import bloodandmithril.generation.patterns.Layers;
import bloodandmithril.generation.patterns.UndergroundWithCaves;
import bloodandmithril.generation.tools.RectangularSpaceCalculator;
import bloodandmithril.generation.tools.SawToothGenerator;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;


/**
 * The structure of a plains surface to be stored and used to generate when needed.
 * 
 * @author Sam, Matt
 */
public class Plains extends SuperStructure {
	private static final long serialVersionUID = -182152409042364632L;
	
	private SawToothGenerator perlinSurfaceGenerator = new SawToothGenerator(plainsMinHeight, plainsMaxHeight, 3, 1, 30);

	@Override
	protected Boundaries findSpace(int startingChunkX, int startingChunkY) {
		//calculates where the structure can go
		return RectangularSpaceCalculator.calculateBoundaries(
			true,
			startingChunkX,
			startingChunkY,
			(plainsMaxWidth - plainsMinWidth) / 2 + 1,
			maxSurfaceHeight - plainsMinHeight / Topography.CHUNK_SIZE + 1,
			maxSurfaceHeight,
			plainsMinHeight / Topography.CHUNK_SIZE - 1
		);		
	}


	@Override
	protected void internalGenerate(boolean generatingToRight) {
		// generate the surface height across the structure.
		int rightMostTile = (boundaries.right + 1) * Topography.CHUNK_SIZE - 1;
		int leftMostTile = boundaries.left * Topography.CHUNK_SIZE;
		if (generatingToRight) {
			for (int x = leftMostTile; x <= rightMostTile; x++) {
				perlinSurfaceGenerator.generateSurfaceHeight(x, generatingToRight, StructureMap.surfaceHeight);
			}
		} else {
			for (int x = rightMostTile; x >= leftMostTile; x--) {
				perlinSurfaceGenerator.generateSurfaceHeight(x, generatingToRight, StructureMap.surfaceHeight);
			}
		}		
	}
	

	@Override
	protected Tile internalGetForegroundTile(int worldTileX, int worldTileY) {
		if (worldTileY > StructureMap.surfaceHeight.get(worldTileX)) {
			return new Tile.EmptyTile();
		} else {
			return UndergroundWithCaves.getTile(worldTileX, worldTileY);
		}
	}
	

	@Override
	protected Tile internalGetBackgroundTile(int worldTileX, int worldTileY) {
		if (worldTileY + 2> StructureMap.surfaceHeight.get(worldTileX)) {
			return new Tile.EmptyTile();
		} else {
			return Layers.getTile(worldTileX, worldTileY);
		}
	}
}