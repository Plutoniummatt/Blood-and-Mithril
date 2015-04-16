package bloodandmithril.generation;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.biome.BiomeDecider;
import bloodandmithril.networking.ClientServerInterface;
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
@Copyright("Matthew Peck 2014")
public class ChunkGenerator implements Serializable {
	private static final long serialVersionUID = -2526181045653733253L;

	/** Decides which biomes to generate */
	private BiomeDecider biomeDecider;

	public static final int maxSurfaceHeightInChunks = 50;


	/**
	 * Constructor
	 */
	public ChunkGenerator(BiomeDecider biomeDecider) {
		this.biomeDecider = biomeDecider;
	}


	/**
	 * Generates a chunk, based on passed in chunk coordinates
	 */
	public void generate(int chunkX, int chunkY, World world, boolean populateChunkMap) {

		// If a structure does not exist where the surface should be, generate the surface structure.
		generateSurface(chunkX, world);

		// If a structure still does not exist at the chunk coordinates being generated, it must be outside of the surface structure boundaries.
		generateAboveAndBelowSurface(chunkX, chunkY, world);

		if (!populateChunkMap) {
			return;
		}

		// Makes arrays of tiles to work on. both for foreground and background
		Tile[][] fTiles = new Tile[Topography.CHUNK_SIZE][Topography.CHUNK_SIZE];
		Tile[][] bTiles = new Tile[Topography.CHUNK_SIZE][Topography.CHUNK_SIZE];

		// Populate the tile arrays.
		populateTileArrays(chunkX, chunkY, fTiles, bTiles, world.getTopography().getStructures());

		// Create the chunk and put it in the ChunkMap.
		Chunk newChunk = new Chunk(fTiles, bTiles, chunkX, chunkY, world.getWorldId());
		world.getTopography().getChunkMap().addChunk(chunkX, chunkY, newChunk);
		placeProps(world, chunkX, chunkY, world.getTopography().getStructures());

		// If the structure has finished generating, we can delete it from the StructureMap, otherwise, decrement the number of chunks left to be generated on the structure
		if (world.getTopography().getStructures().structureExists(chunkX, chunkY, true)) {
			world.getTopography().getStructures().deleteChunkFromStructureKeyMapAndCheckIfStructureCanBeDeleted(chunkX, chunkY, true);
		}
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


	/** Adds props */
	private void placeProps(World world, int x, int y, Structures structures) {
		if (world.getTopography().getStructures().structureExists(x, y, true)) {
			Structures.get(world.getTopography().getStructures().getSuperStructureKeys().get(x).get(y)).attemptPropPlacement(x, y);
		}
		if (world.getTopography().getStructures().structureExists(x, y, false)) {
			Structures.get(world.getTopography().getStructures().getSubStructureKeys().get(x).get(y)).attemptPropPlacement(x, y);
		}
	}


	/** Handles the generation of non-surface structures */
	private void generateAboveAndBelowSurface(int x, int y, World world) {
		if (!world.getTopography().getStructures().structureExists(x, y, true)) {
			if (y < maxSurfaceHeightInChunks) {
				biomeDecider.decideAndGetSubterraneanBiome(world).generate(x, y, true);
			} else {
				biomeDecider.decideAndGetElevatedBiome(world).generate(x, y, true);
			}
		}
	}


	/** Handles the generation of surface structures */
	private void generateSurface(int chunkX, World world) {
		for (int tempX = chunkX - 5; tempX <= chunkX + 5; tempX++) {
			boolean generatingToRight = tempX >= chunkX;

			if (!world.getTopography().getStructures().structureExists(tempX, maxSurfaceHeightInChunks, true) && !world.getTopography().getChunkMap().doesChunkExist(tempX, maxSurfaceHeightInChunks)) {
				biomeDecider.decideAndGetSurfaceBiome(world).generate(tempX, maxSurfaceHeightInChunks, generatingToRight);
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