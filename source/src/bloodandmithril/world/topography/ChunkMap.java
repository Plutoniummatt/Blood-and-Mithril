package bloodandmithril.world.topography;

import static com.google.common.collect.Maps.newHashMap;

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
	public Map<Integer, Map<Integer, Chunk>> chunkMap;

	/**
	 * Constructor
	 */
	public ChunkMap() {
		chunkMap = newHashMap();
	}


	/**
	 * Puts a column of chunks into the chunkMap
	 */
	public final synchronized void putColumn(final int x, final Map<Integer, Chunk> chunks) {
		chunkMap.put(x, chunks);
	}


	/**
	 * @param chunkX - x-world chunk coord
	 * @param chunkY - y-world chunk coord
	 * @return whether or not a chunk at chunk coordinates x, y exists.
	 */
	public final synchronized boolean doesChunkExist(final int chunkX, final int chunkY) {
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
	public final boolean doesChunkExist(final float x, final float y) {
		final int chunkX = Topography.convertToChunkCoord(x);
		final int chunkY = Topography.convertToChunkCoord(y);

		return doesChunkExist(chunkX, chunkY);
	}


	/**
	 * @param x - x-world chunk coord
	 * @param y - y-world chunk coord
	 * @return whether or not a chunk at chunk coordinates x, y exists.
	 */
	public final boolean doesChunkExist(final Vector2 location) {
		return doesChunkExist(location.x, location.y);
	}


	/**
	 * @param chunkX - the x chunk coordinate of the chunk column you want to get.
	 * @return the column of chunks you wanted.
	 */
	public final synchronized Map<Integer, Chunk> get(final int chunkX) {
		return chunkMap.get(chunkX);
	}


	/**
	 * See {@link #chunkMap}
	 */
	public final synchronized Map<Integer, Map<Integer, Chunk>> getChunkMap() {
		return chunkMap;
	}


	/**
	 * Adds a chunk to the chunkMap
	 */
	public final synchronized Chunk addChunk(final int chunkX, final int chunkY, final Chunk chunk) {
		if (chunkMap.get(chunkX) == null) {
			chunkMap.put(chunkX, new HashMap<Integer, Chunk>());
		}
		return chunkMap.get(chunkX).put(chunkY, chunk);
	}
}
