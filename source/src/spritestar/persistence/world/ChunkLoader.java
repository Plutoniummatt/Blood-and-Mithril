package spritestar.persistence.world;


import spritestar.world.topography.Topography;

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
	public void load(Topography topography, int chunkX, int chunkY);
}
