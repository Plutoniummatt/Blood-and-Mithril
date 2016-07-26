package bloodandmithril.generation;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Adds the chunks to the world
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ChunkPopulator {

	
	/**
	 * Adds the chunk to the world
	 */
	public void populateChunkMap(Tile[][] fTiles, Tile[][] bTiles, World world, int chunkX, int chunkY) {
		// Create the chunk and put it in the ChunkMap.
		final Chunk newChunk = new Chunk(fTiles, bTiles, chunkX, chunkY, world.getWorldId());
		world.getTopography().getChunkMap().addChunk(chunkX, chunkY, newChunk);
		placeProps(world, chunkX, chunkY, world.getTopography().getStructures());

		// If the structure has finished generating, we can delete it from the StructureMap, otherwise, decrement the number of chunks left to be generated on the structure
		if (world.getTopography().getStructures().structureExists(chunkX, chunkY, true)) {
			world.getTopography().getStructures().deleteChunkFromStructureKeyMapAndCheckIfStructureCanBeDeleted(chunkX, chunkY, true);
		}
	}
	
	
	/** Adds props */
	private void placeProps(final World world, final int x, final int y, final Structures structures) {
		if (world.getTopography().getStructures().structureExists(x, y, true)) {
			Structures.get(world.getTopography().getStructures().getSuperStructureKeys().get(x).get(y)).attemptPropPlacement(x, y);
		}
		if (world.getTopography().getStructures().structureExists(x, y, false)) {
			Structures.get(world.getTopography().getStructures().getSubStructureKeys().get(x).get(y)).attemptPropPlacement(x, y);
		}
	}
}
