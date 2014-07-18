package bloodandmithril.generation.superstructure;

import static bloodandmithril.generation.settings.GlobalGenerationSettings.hillsMaxHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.hillsMaxWidth;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.hillsMinHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.hillsMinWidth;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.maxSurfaceHeight;
import bloodandmithril.core.Copyright;
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
 * The structure of a hill surface to be stored and used to generate when needed.
 *
 * @author Sam, Matt
 */
@Copyright("Matthew Peck 2014")
public class Hills extends SuperStructure {
	private static final long serialVersionUID = -2162566918693231667L;

	/**
	 * Constructor
	 */
	public Hills(int worldId) {
		super(worldId);
	}


	@Override
	protected Boundaries findSpace(int startingChunkX, int startingChunkY) {
		//calculates where the structure can go
		return RectangularSpaceCalculator.calculateBoundaries(
			true,
			startingChunkX,
			startingChunkY,
			(hillsMaxWidth - hillsMinWidth) / 2 + 1,
			maxSurfaceHeight - hillsMinHeight / Topography.CHUNK_SIZE + 1,
			maxSurfaceHeight,
			hillsMinHeight / Topography.CHUNK_SIZE - 1,
			Domain.getWorld(worldId).getTopography()
		);
	}


	@Override
	protected void internalGenerate(boolean generatingToRight) {
		Structures structures = Domain.getWorld(worldId).getTopography().getStructures();
		SawToothGenerator surfaceGenerator = new SawToothGenerator(hillsMinHeight, hillsMaxHeight, 0, 0, 30);

		// generate the surface height across the structure.
		int rightMostTile = (getBoundaries().right + 1) * Topography.CHUNK_SIZE - 1;
		int leftMostTile = getBoundaries().left * Topography.CHUNK_SIZE;
		if (generatingToRight) {
			for (int x = leftMostTile; x <= rightMostTile; x++) {
				if (x == rightMostTile - 40) {
					surfaceGenerator.setMaxSurface(hillsMinHeight);
				}
				surfaceGenerator.generateSurfaceHeight(x, generatingToRight, structures.getSurfaceHeight());
			}
		} else {
			for (int x = rightMostTile; x >= leftMostTile; x--) {
				if (x == leftMostTile + 40) {
					surfaceGenerator.setMaxSurface(hillsMinHeight);
				}
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
		if (worldTileY + 2 > structures.getSurfaceHeight().get(worldTileX)) {
			return new Tile.EmptyTile();
		} else {
			return Layers.getTile(worldTileX, worldTileY);
		}
	}
}