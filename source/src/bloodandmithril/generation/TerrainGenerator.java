package bloodandmithril.generation;

import static bloodandmithril.generation.settings.GlobalGenerationSettings.maxStructureWidth;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.maxSurfaceHeight;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.generation.superstructure.Caves;
import bloodandmithril.generation.tools.BiomeDecider;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Entry point class for terrain generation
 *
 * @author Matt
 */
public class TerrainGenerator {

	/** Decides which biomes to generate */
	private BiomeDecider biomeDecider = new BiomeDecider();

	/**
	 * Generates a chunk, based on passed in chunk coordinates
	 */
	public void generate(int x, int y, World world) {
		// Makes arrays of tiles to work on. both for foreground and background
		Tile[][] fTiles = new Tile[Topography.CHUNK_SIZE][Topography.CHUNK_SIZE];
		Tile[][] bTiles = new Tile[Topography.CHUNK_SIZE][Topography.CHUNK_SIZE];

		// If a structure does not exist where the surface should be, generate the surface structure.
		generateSurface(x, world);

		// If a structure still does not exist at the chunk coordinates being generated, it must be outside of the surface structure boundaries.
		generateAboveAndBelowSurface(x, y, world);

		// Populate the tile arrays.
		populateTileArrays(x, y, fTiles, bTiles, world.getTopography().getStructures());
		
		// If the structure has finished generating, we can delete it from the StructureMap, otherwise, decrement the number of chunks left to be generated on the structure
		if (world.getTopography().getStructures().structureExists(x, y, true)) {
			world.getTopography().getStructures().deleteChunkFromStructureKeyMapAndCheckIfStructureCanBeDeleted(x, y, true);
		}

		// Create the chunk and put it in the ChunkMap.
		Chunk newChunk = new Chunk(fTiles, bTiles, x, y, world.getWorldId());
		world.getTopography().getChunkMap().addChunk(x, y, newChunk);
	}


	/** Populates tile arrays with tiles, determined by the structures */
	private void populateTileArrays(int x, int y, Tile[][] fTiles, Tile[][] bTiles, Structures structures) {
		for (int tileX = 0; tileX < Topography.CHUNK_SIZE; tileX++) {
			for (int tileY = 0; tileY < Topography.CHUNK_SIZE; tileY++) {
				try {
					// Get the background tile from the structure
					bTiles[tileX][tileY] = structures.getTile(x, y, Topography.convertToWorldTileCoord(x, tileX), Topography.convertToWorldTileCoord(y, tileY), false);
				} catch (NullPointerException e) {
					handleNPE(bTiles, tileX, tileY, e);
				}

				try {
					// Get the foreground tile from the structure
					fTiles[tileX][tileY] = structures.getTile(x, y, Topography.convertToWorldTileCoord(x, tileX), Topography.convertToWorldTileCoord(y, tileY), true);
				} catch (NullPointerException e) {
					handleNPE(fTiles, tileX, tileY, e);
				}
			}
		}
	}


	/** Handles the generation of non-surface structures */
	private void generateAboveAndBelowSurface(int x, int y, World world) {
		if (!world.getTopography().getStructures().structureExists(x, y, true)) {
			if (y < maxSurfaceHeight) {
				Caves caves = new Caves(world.getWorldId()); // TODO make this procedural, not hard coded
				caves.generate(x, y, true);
			} else {
				// TODO above surface structure
			}
		}
	}


	/** Handles the generation of surface structures */
	private void generateSurface(int x, World world) {
		for (int tempX = x - maxStructureWidth; tempX <= x	+ maxStructureWidth; tempX++) {
			boolean generatingToRight = tempX >= x;

			if (!world.getTopography().getStructures().structureExists(tempX, maxSurfaceHeight, true) && !world.getTopography().getChunkMap().doesChunkExist(tempX, maxSurfaceHeight)) {
				biomeDecider.decideAndGetBiome(world).generate(tempX, maxSurfaceHeight, generatingToRight);
			}
		}
	}


	/** Handles a {@link NullPointerException} during generation */
	private void handleNPE(Tile[][] bTiles, int tileX, int tileY, NullPointerException e) {
		if (!ClientServerInterface.isClient() && UserInterface.DEBUG) {
			bTiles[tileX][tileY] = new Tile.DebugTile();
		} else {
			throw new RuntimeException("Got an NPE during generation", e);
		}
	}
}