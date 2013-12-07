package bloodandmithril.world.topography;

import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.math.Vector2;

/**
 * Contains a list of an array of chunks. The map of tiles.
 */
public class ChunkMap {

	/** The chunk map - an array list of columns */
	public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Chunk>> chunkMap;

	/**
	 * Constructor
	 */
	public ChunkMap() {
		chunkMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Chunk>>();
	}


	/**
	 * Puts a column of chunks into the chunkMap
	 */
	public void putColumn(int x, ConcurrentHashMap<Integer, Chunk> chunks) {
		chunkMap.put(x, chunks);
	}


	/**
	 * @param chunkX - x-world chunk coord
	 * @param chunkY - y-world chunk coord
	 * @return whether or not a chunk at chunk coordinates x, y exists.
	 */
	public boolean doesChunkExist(int chunkX, int chunkY) {
		if (chunkMap.get(chunkX) == null) {
			return false;
		} else {
			return chunkMap.get(chunkX).get(chunkY) != null;
		}
	}


	/**
	 * @param x - x-world coord
	 * @param y - y-world coord
	 * @return whether or not a chunk at chunk coordinates x, y exists.
	 */
	public boolean doesChunkExist(float x, float y) {
		int chunkX = Topography.convertToChunkCoord(x);
		int chunkY = Topography.convertToChunkCoord(y);

		return doesChunkExist(chunkX, chunkY);
	}


	/**
	 * @param x - x-world chunk coord
	 * @param y - y-world chunk coord
	 * @return whether or not a chunk at chunk coordinates x, y exists.
	 */
	public boolean doesChunkExist(Vector2 location) {
		return doesChunkExist(location.x, location.y);
	}


	/**
	 * @param chunkX - the x chunk coordinate of the chunk column you want to get.
	 * @return the column of chunks you wanted.
	 */
	public ConcurrentHashMap<Integer, Chunk> get(int chunkX) {
		return chunkMap.get(chunkX);
	}


	/**
	 * Adds a chunk to the chunkMap
	 */
	public Chunk addChunk(int chunkX, int chunkY, Chunk chunk) {
		if (chunkMap.get(chunkX) == null) {
			chunkMap.put(chunkX, new ConcurrentHashMap<Integer, Chunk>());
		}
		return chunkMap.get(chunkX).put(chunkY, chunk);
	}
}
