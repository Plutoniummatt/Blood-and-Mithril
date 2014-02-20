package bloodandmithril.generation;

import static bloodandmithril.generation.settings.GlobalGenerationSettings.maxStructureWidth;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.maxSurfaceHeight;
import bloodandmithril.generation.superstructure.Caves;
import bloodandmithril.generation.tools.BiomeDecider;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Implementation of {@link TerrainGenerator}
 *
 * @author Sam, Matt
 */
public class TerrainGenerator {

	/** Decides which biomes to generate */
	BiomeDecider biomeDecider = new BiomeDecider();

	/**
	 * Generates a chunk, based on passed in chunk coordinates
	 */
	public void generate(int x, int y) {
		// Makes arrays of tiles to work on. both for foreground and background
		Tile[][] fTiles = new Tile[Topography.CHUNK_SIZE][Topography.CHUNK_SIZE];
		Tile[][] bTiles = new Tile[Topography.CHUNK_SIZE][Topography.CHUNK_SIZE];

		// If a structure does not exist where the surface should be, generate the surface structure.
		generateSurface(x);

		// If a structure still does not exist at the chunk coordinates being generated, it must be outside of the surface structure boundaries.
		generateAboveAndBelowSurface(x, y);

		// Populate the tile arrays.
		populateTileArrays(x, y, fTiles, bTiles);

		// Create the chunk and put it in the ChunkMap.
		Chunk newChunk = new Chunk(fTiles, bTiles, x, y);
		Topography.chunkMap.addChunk(x, y, newChunk);
	}


	/** Populates tile arrays with tiles, determined by the structures */
	private void populateTileArrays(int x, int y, Tile[][] fTiles, Tile[][] bTiles) {
		for (int tileX = 0; tileX < Topography.CHUNK_SIZE; tileX++) {
			for (int tileY = 0; tileY < Topography.CHUNK_SIZE; tileY++) {
				try {
					// Get the background tile from the structure
					bTiles[tileX][tileY] = StructureMap.getTile(x, y, Topography.convertToWorldTileCoord(x, tileX), Topography.convertToWorldTileCoord(y, tileY), false);
				} catch (NullPointerException e) {
					handleNPE(bTiles, tileX, tileY, e);
				}

				try {
					// Get the foreground tile from the structure
					fTiles[tileX][tileY] = StructureMap.getTile(x, y, Topography.convertToWorldTileCoord(x, tileX), Topography.convertToWorldTileCoord(y, tileY), true);
				} catch (NullPointerException e) {
					handleNPE(fTiles, tileX, tileY, e);
				}
			}
		}

		// If the structure has finished generating, we can delete it from the StructureMap.
		if (StructureMap.doesStructureExist(x, y, true)) {
			StructureMap.structureDeletionCheck(x, y, true);
		}
	}


	/** Handles the generation of non-surface structures */
	private void generateAboveAndBelowSurface(int x, int y) {
		if (!StructureMap.doesStructureExist(x, y, true)) {
			if (y < maxSurfaceHeight) {
				Caves caves = new Caves(); // TODO make this procedural, not hard coded
				caves.generate(x, y, true);
			} else {
				// TODO above surface structure
			}
		}
	}


	/** Handles the generation of surface structures */
	private void generateSurface(int x) {
		for (int tempX = x - maxStructureWidth; tempX <= x	+ maxStructureWidth; tempX++) {
			boolean generatingToRight = tempX >= x;

			if (!StructureMap.doesStructureExist(tempX, maxSurfaceHeight, true) && !Topography.chunkMap.doesChunkExist(tempX, maxSurfaceHeight)) {
				biomeDecider.decideAndGetBiome().generate(tempX, maxSurfaceHeight, generatingToRight);
			}
		}
	}


	/** Handles a {@link NullPointerException} during generation */
	private void handleNPE(Tile[][] bTiles, int tileX, int tileY, NullPointerException e) {
		if (System.getProperty("debug").equals("true")) {
			bTiles[tileX][tileY] = new Tile.DebugTile();
		} else {
			throw new RuntimeException(e);
		}
	}
}