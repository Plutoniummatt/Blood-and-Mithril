package spritestar.generation;

import spritestar.world.topography.Chunk;
import spritestar.world.topography.Topography;
import spritestar.world.topography.tile.Tile;

/**
 * {@link TerrainGenerator}, responsible for generating the world
 *
 * @author Matt
 */
public class TerrainGenerator {

	public void generate(int chunkX, int chunkY) {

		// Makes arrays of tiles to work on. both for foreground and background
		Tile[][] fTiles = new Tile[Topography.chunkSize][Topography.chunkSize];
		Tile[][] bTiles = new Tile[Topography.chunkSize][Topography.chunkSize];

		// Checks if we have a structure underneath, if we do, no need to generate, otherwise generate
		checkStructures(chunkX, chunkY);

		// Populate the tile arrays.
		populateTileArrays(chunkX, chunkY, fTiles, bTiles);

		// Create the chunk and put it in the ChunkMap.
		Chunk newChunk = new Chunk(fTiles, bTiles, chunkX, chunkY);
		Topography.chunkMap.addChunk(chunkX, chunkY, newChunk);
	}


	/** Generates structures if non exist underneath current position */
	private void checkStructures(int chunkX, int chunkY) {
		Structure superStructure = Structures.getStructure(chunkX, chunkY, true);

		// If we do not find a superStructure here, generate super + sub structures
		if (superStructure == null) {
			// TODO generate
		} else {
			return;
		}
	}


	/** Populates tile arrays with tiles, determined by the structures */
	private void populateTileArrays(int chunkX, int chunkY, Tile[][] fTiles, Tile[][] bTiles) {
		for (int tileX = 0; tileX < Topography.chunkSize; tileX++) {
			for (int tileY = 0; tileY < Topography.chunkSize; tileY++) {

				// Obtain the structures underneath our current position
				Structure superStructure = Structures.getStructure(chunkX, chunkY, true);
				Structure subStructure = Structures.getStructure(chunkX, chunkY, false);

				// Get the tiles, from substructure first, failing that, fall back to superStructure
				Class<? extends Tile> fTile = null;
				if (subStructure != null) {
					fTile = subStructure.getForegroundTile(tileX, tileY);
					if (fTile == null) {
						fTile = superStructure.getForegroundTile(tileX, tileY);
					}
				}

				Class<? extends Tile> bTile = null;
				if (subStructure != null) {
					bTile = subStructure.getBackgroundTile(tileX, tileY);
					if (fTile == null) {
						bTile = superStructure.getBackgroundTile(tileX, tileY);
					}
				}

				// Put tile into array
				try {
					fTiles[tileX][tileY] = fTile == null ? new Tile.DebugTile() : fTile.newInstance();
					bTiles[tileX][tileY] = bTile == null ? new Tile.DebugTile() : bTile.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}