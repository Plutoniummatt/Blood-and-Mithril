package bloodandmithril.world.topography;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;

/**
 * Contains a list of an array of chunks. The map of tiles.
 */
@Copyright("Matthew Peck 2014")
public final class ChunkMap {

	/** The chunk map - an array list of columns */
	public HashMap<Integer, HashMap<Integer, Chunk>> chunkMap;

	/**
	 * Constructor
	 */
	public ChunkMap() {
		chunkMap = new HashMap<Integer, HashMap<Integer, Chunk>>();
	}


	/**
	 * Puts a column of chunks into the chunkMap
	 */
	public final synchronized void putColumn(int x, HashMap<Integer, Chunk> chunks) {
		chunkMap.put(x, chunks);
	}


	/**
	 * @param chunkX - x-world chunk coord
	 * @param chunkY - y-world chunk coord
	 * @return whether or not a chunk at chunk coordinates x, y exists.
	 */
	public final synchronized boolean doesChunkExist(int chunkX, int chunkY) {
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
	public final boolean doesChunkExist(float x, float y) {
		int chunkX = Topography.convertToChunkCoord(x);
		int chunkY = Topography.convertToChunkCoord(y);

		return doesChunkExist(chunkX, chunkY);
	}


	/**
	 * @param x - x-world chunk coord
	 * @param y - y-world chunk coord
	 * @return whether or not a chunk at chunk coordinates x, y exists.
	 */
	public final boolean doesChunkExist(Vector2 location) {
		return doesChunkExist(location.x, location.y);
	}


	/**
	 * @param chunkX - the x chunk coordinate of the chunk column you want to get.
	 * @return the column of chunks you wanted.
	 */
	public final synchronized HashMap<Integer, Chunk> get(int chunkX) {
		return chunkMap.get(chunkX);
	}


	/**
	 * See {@link #chunkMap}
	 */
	public final synchronized Map<Integer, HashMap<Integer, Chunk>> getChunkMap() {
		return chunkMap;
	}


	/**
	 * Adds a chunk to the chunkMap
	 */
	public final synchronized Chunk addChunk(int chunkX, int chunkY, Chunk chunk) {
		if (chunkMap.get(chunkX) == null) {
			chunkMap.put(chunkX, new HashMap<Integer, Chunk>());
		}
		return chunkMap.get(chunkX).put(chunkY, chunk);
	}
}
