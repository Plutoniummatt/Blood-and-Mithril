package bloodandmithril.generation.superstructure;

import static bloodandmithril.generation.settings.GlobalGenerationSettings.maxSurfaceHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.plainsMaxHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.plainsMaxWidth;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.plainsMinHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.plainsMinWidth;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.patterns.Layers;
import bloodandmithril.generation.patterns.UndergroundWithCaves;
import bloodandmithril.generation.tools.RectangularSpaceCalculator;
import bloodandmithril.generation.tools.SawToothGenerator;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;


/**
 * The structure of a plains surface to be stored and used to generate when needed.
 *
 * @author Sam, Matt
 */
public class Plains extends SuperStructure {
	private static final long serialVersionUID = -182152409042364632L;

	private final SawToothGenerator surfaceGenerator = new SawToothGenerator(plainsMinHeight, plainsMaxHeight, 3, 1, 30);

	/**
	 * Constructor
	 */
	public Plains(int worldId) {
		super(worldId);
	}
	
	
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
			plainsMinHeight / Topography.CHUNK_SIZE - 1,
			Domain.getWorld(worldId).getTopography()
		);
	}


	@Override
	protected void internalGenerate(boolean generatingToRight) {
		Structures structures = Domain.getWorld(worldId).getTopography().getStructures();
		// generate the surface height across the structure.
		int rightMostTile = (getBoundaries().right + 1) * Topography.CHUNK_SIZE - 1;
		int leftMostTile = getBoundaries().left * Topography.CHUNK_SIZE;
		if (generatingToRight) {
			for (int x = leftMostTile; x <= rightMostTile; x++) {
				surfaceGenerator.generateSurfaceHeight(x, generatingToRight, structures.getSurfaceHeight());
			}
		} else {
			for (int x = rightMostTile; x >= leftMostTile; x--) {
				surfaceGenerator.generateSurfaceHeight(x, generatingToRight, structures.getSurfaceHeight());
			}
		}
	}


	@Override
	protected Tile internalGetForegroundTile(int worldTileX, int worldTileY) {
		Structures structures = Domain.getWorld(worldId).getTopography().getStructures();
		if (worldTileY > structures.getSurfaceHeight().get(worldTileX)) {
			return new Tile.EmptyTile();
		} else {
			return UndergroundWithCaves.getTile(worldTileX, worldTileY);
		}
	}


	@Override
	protected Tile internalGetBackgroundTile(int worldTileX, int worldTileY) {
		Structures structures = Domain.getWorld(worldId).getTopography().getStructures();
		if (worldTileY + 2> structures.getSurfaceHeight().get(worldTileX)) {
			return new Tile.EmptyTile();
		} else {
			return Layers.getTile(worldTileX, worldTileY);
		}
	}
}