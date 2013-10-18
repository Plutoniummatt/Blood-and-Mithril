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

		// Populate the tile arrays.
		populateTileArrays(chunkX, chunkY, fTiles, bTiles);

		// Create the chunk and put it in the ChunkMap.
		Chunk newChunk = new Chunk(fTiles, bTiles, chunkX, chunkY);
		Topography.chunkMap.addChunk(chunkX, chunkY, newChunk);
	}


	/** Populates tile arrays with tiles, determined by the structures */
	private void populateTileArrays(int chunkX, int chunkY, Tile[][] fTiles, Tile[][] bTiles) {
		for (int tileX = 0; tileX < Topography.chunkSize; tileX++) {
			for (int tileY = 0; tileY < Topography.chunkSize; tileY++) {
				// TODO populateTileArrays
				fTiles[tileX][tileY] = new Tile.EmptyTile();
				bTiles[tileX][tileY] = new Tile.EmptyTile();
			}
		}
	}
}