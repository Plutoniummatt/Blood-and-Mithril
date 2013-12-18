package bloodandmithril.persistence.world;


import bloodandmithril.world.topography.Topography;

/**
 * Interface for loading chunks from disk
 *
 * @author Matt
 */
public interface ChunkLoader {

	/**
	 * @param chunkX - Chunk x-coord to load
	 * @param chunkY - Chunk y-coord to load
	 *
	 * @return whether or not the load was successful
	 */
	public boolean load(Topography topography, int chunkX, int chunkY);
}
