package bloodandmithril.generation;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Entry point class for terrain generation
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class ChunkGenerator {

	@Inject private UserInterface userInterface;

	public static final int maxSurfaceHeightInChunks = 50;

	/**
	 * Generates a chunk, based on passed in chunk coordinates
	 */
	public WrapperForTwo<Tile[][], Tile[][]> generate(final int chunkX, final int chunkY, final World world) {

		// If a structure does not exist where the surface should be, generate the surface structure.
		generateSurface(chunkX, world);

		// If a structure still does not exist at the chunk coordinates being generated, it must be outside of the surface structure boundaries.
		generateAboveAndBelowSurface(chunkX, chunkY, world);

		// Makes arrays of tiles to work on. both for foreground and background
		final Tile[][] fTiles = new Tile[Topography.CHUNK_SIZE][Topography.CHUNK_SIZE];
		final Tile[][] bTiles = new Tile[Topography.CHUNK_SIZE][Topography.CHUNK_SIZE];

		// Populate the tile arrays.
		populateTileArrays(chunkX, chunkY, fTiles, bTiles, world.getTopography().getStructures());
		
		return WrapperForTwo.wrap(fTiles, bTiles);
	}


	/** Populates tile arrays with tiles, determined by the structures */
	private void populateTileArrays(final int x, final int y, final Tile[][] fTiles, final Tile[][] bTiles, final Structures structures) {
		for (int tileX = 0; tileX < Topography.CHUNK_SIZE; tileX++) {
			for (int tileY = 0; tileY < Topography.CHUNK_SIZE; tileY++) {
				try {
					// Get the background tile from the structure
					bTiles[tileX][tileY] = structures.getTile(x, y, Topography.convertToWorldTileCoord(x, tileX), Topography.convertToWorldTileCoord(y, tileY), false);
				} catch (final NullPointerException e) {
					handleNPE(bTiles, tileX, tileY, e);
				}

				try {
					// Get the foreground tile from the structure
					fTiles[tileX][tileY] = structures.getTile(x, y, Topography.convertToWorldTileCoord(x, tileX), Topography.convertToWorldTileCoord(y, tileY), true);
				} catch (final NullPointerException e) {
					handleNPE(fTiles, tileX, tileY, e);
				}
			}
		}
	}


	/** Handles the generation of non-surface structures */
	private void generateAboveAndBelowSurface(final int x, final int y, final World world) {
		if (!world.getTopography().getStructures().structureExists(x, y, true)) {
			if (y < maxSurfaceHeightInChunks) {
				Wiring.injector().getInstance(world.getBiomeDecider()).decideAndGetSubterraneanBiome(world).generate(x, y, true);
			} else {
				Wiring.injector().getInstance(world.getBiomeDecider()).decideAndGetElevatedBiome(world).generate(x, y, true);
			}
		}
	}


	/** Handles the generation of surface structures */
	private void generateSurface(final int chunkX, final World world) {
		for (int tempX = chunkX - 5; tempX <= chunkX + 5; tempX++) {
			final boolean generatingToRight = tempX >= chunkX;

			if (!world.getTopography().getStructures().structureExists(tempX, maxSurfaceHeightInChunks, true) && !world.getTopography().getChunkMap().doesChunkExist(tempX, maxSurfaceHeightInChunks)) {
				Wiring.injector().getInstance(world.getBiomeDecider()).decideAndGetSurfaceBiome(world).generate(tempX, maxSurfaceHeightInChunks, generatingToRight);
			}
		}
	}


	/** Handles a {@link NullPointerException} during generation */
	private void handleNPE(final Tile[][] bTiles, final int tileX, final int tileY, final NullPointerException e) {
		if (!ClientServerInterface.isClient() && userInterface.DEBUG) {
			bTiles[tileX][tileY] = new Tile.DebugTile();
		} else {
			throw new RuntimeException("Got an NPE during generation", e);
		}
	}
}