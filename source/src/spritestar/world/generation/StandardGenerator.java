package spritestar.world.generation;

import static spritestar.world.generation.settings.GlobalGenerationSettings.*;

import spritestar.world.generation.superstructures.Caves;
import spritestar.world.generation.tools.BiomeDecider;
import spritestar.world.topography.Chunk;
import spritestar.world.topography.Topography;
import spritestar.world.topography.tile.Tile;

/**
 * Implementation of {@link TerrainGenerator}
 *
 * @author Sam, Matt
 */
public class StandardGenerator extends TerrainGenerator {
	
	/** Decides which biomes to generate */
	BiomeDecider biomeDecider = new BiomeDecider();

	@Override
	public void generate(int x, int y) {
		
		// Makes arrays of tiles to work on. both for foreground and background
		Tile[][] fTiles = new Tile[Topography.chunkSize][Topography.chunkSize];
		Tile[][] bTiles = new Tile[Topography.chunkSize][Topography.chunkSize];
		
		// If there isn't a structure where the surface should be above the chunk we're generating, make the surface structure.
		handleSurface(x);
		
		// If there still isn't a structure on the chunk we're generating, it's either sky or underground.
		handleSkyAndUnderground(x, y);
		
		// Populate the tile arrays.
		populateTileArrays(x, y, fTiles, bTiles);
		
		// Create the chunk and put it in the ChunkMap.
		Chunk newChunk = new Chunk(fTiles, bTiles, x, y);
		Topography.chunkMap.addChunk(x, y, newChunk);
	}


	/** Populates tile arrays with tiles, determined by the structures */
	private void populateTileArrays(int x, int y, Tile[][] fTiles, Tile[][] bTiles) {
		for (int tileX = 0; tileX < Topography.chunkSize; tileX++) {
			for (int tileY = 0; tileY < Topography.chunkSize; tileY++) {
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
		if (StructureMap.doesSuperStructureExist(x, y)) {
			StructureMap.deleteSuperStructure(x, y);
		}
	}


	/** Handles the generation of non-surface structures */
	private void handleSkyAndUnderground(int x, int y) {
		if (!StructureMap.doesSuperStructureExist(x, y)) {
			if (y < maxSurfaceHeight) {
				Caves caves = new Caves();
				caves.generateAndFinalize(x, y, true);
			}
		}
	}


	/** Handles the generation of surface structures */
	private void handleSurface(int x) {
		for (int tempX = x - maxStructureWidth; tempX <= x	+ maxStructureWidth; tempX++) {
			boolean generatingToRight = tempX >= x;
			
			if (!StructureMap.doesSuperStructureExist(tempX, maxSurfaceHeight) && !Topography.chunkMap.doesChunkExist(tempX, maxSurfaceHeight)) {
				biomeDecider.decideAndGetBiome().generateAndFinalize(tempX, maxSurfaceHeight, generatingToRight);
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