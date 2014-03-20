package bloodandmithril.world.topography;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.lwjgl.opengl.Display;

import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.generation.Structures;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.persistence.world.ChunkLoaderImpl;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents the topography of the gameworld - Chunks, Tiles, and objects that
 * exist within it etc.
 *
 * @author Matt
 */
public class Topography {
	
	/** Unique ID of the {@link World} that this {@link Topography} lives on */
	private final int worldId;

	/** The size of a single tile, in pixels (single dimension) */
	public static final int TILE_SIZE = 16;

	/** The size of a chunk, in number of tiles (single dimension) */
	public static final int CHUNK_SIZE = 20;

	/** The texture atlas containing all textures for tiles */
	public static Texture atlas;

	/** The texture coordinate increment representing one tile in the texture atlas. (1/128) */
	public static final float textureCoordinateQuantization = 0.0078125f;

	/** The chunk map of the topography. */
	private final ChunkMap chunkMap;
	
	private final Structures structures;

	/** The chunk loader. */
	private static final ChunkLoader chunkLoader = new ChunkLoaderImpl();

	/** Any non-main thread topography tasks queued here */
	private static BlockingQueue<Task> topographyTasks = new ArrayBlockingQueue<Task>(500000);

	/** The current chunk coordinates that have already been requested for generation */
	private final ConcurrentDualKeyHashMap<Integer, Integer, Boolean> requestedForGeneration = new ConcurrentDualKeyHashMap<>();


	/**
	 * @param generator - The type of generator to use
	 */
	public Topography(int worldId) {
		this.worldId = worldId;
		this.chunkMap = new ChunkMap();
		this.structures = new Structures();
	}


	/** Adds a task to be processed */
	public static void addTask(Task task) {
		synchronized(topographyTasks) {
			topographyTasks.add(task);
		}
	}


	/** Executes any tasks queued in {@link #topographyTasks} by other threads */
	public static void executeBackLog() {
		synchronized(topographyTasks) {
			while (!topographyTasks.isEmpty()) {
				topographyTasks.poll().execute();
			}
		}
	}


	/**
	 * Renders the background
	 */
	public void renderBackGround(int camX, int camY) {
		int bottomLeftX 	= (camX - Display.getWidth() / 2) / (CHUNK_SIZE * TILE_SIZE);
		int bottomLeftY 	= (camY - Display.getHeight() / 2) / (CHUNK_SIZE * TILE_SIZE);
		int topRightX 		= bottomLeftX + Display.getWidth() / (CHUNK_SIZE * TILE_SIZE);
		int topRightY		= bottomLeftY + Display.getHeight() / (CHUNK_SIZE * TILE_SIZE);

		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		for (int x = bottomLeftX - 2; x <= topRightX + 2; x++) {
			for (int y = bottomLeftY - 2; y <= topRightY + 2; y++) {
				if (getChunkMap().get(x) != null && getChunkMap().get(x).get(y) != null) {
					getChunkMap().get(x).get(y).checkMesh();
					getChunkMap().get(x).get(y).render(false);
				}
			}
		}
	}


	/**
	 * Renders the foreground
	 */
	public void renderForeGround(int camX, int camY) {
		int bottomLeftX 	= (camX - Display.getWidth() / 2) / (CHUNK_SIZE * TILE_SIZE);
		int bottomLeftY 	= (camY - Display.getHeight() / 2) / (CHUNK_SIZE * TILE_SIZE);
		int topRightX 		= bottomLeftX + Display.getWidth() / (CHUNK_SIZE * TILE_SIZE);
		int topRightY		= bottomLeftY + Display.getHeight() / (CHUNK_SIZE * TILE_SIZE);

		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		for (int x = bottomLeftX - 2; x <= topRightX + 2; x++) {
			for (int y = bottomLeftY - 2; y <= topRightY + 2; y++) {
				if (getChunkMap().get(x) != null && getChunkMap().get(x).get(y) != null) {
					getChunkMap().get(x).get(y).checkMesh();
					getChunkMap().get(x).get(y).render(true);
				}
			}
		}
	}


	/** Get the lowest empty tile world coordinates */
	public synchronized Vector2 getLowestEmptyOrPlatformTileWorldCoords(float worldX, float worldY, boolean floor) {
		return getLowestEmptyTileOrPlatformTileWorldCoords(new Vector2(worldX, worldY), floor);
	}


	/** Get the lowest empty tile world coordinates */
	public synchronized Vector2 getLowestEmptyTileOrPlatformTileWorldCoords(Vector2 worldCoords, boolean floor) {
		float x = worldCoords.x;
		float y = worldCoords.y;

		if (getTile(worldCoords, true) instanceof EmptyTile) {
			while (getTile(x, y, true) instanceof EmptyTile) {
				y = y - TILE_SIZE;
			}
			y = y + TILE_SIZE;
		} else {
			while (!(getTile(x, y, true) instanceof EmptyTile)) {
				y = y + TILE_SIZE;
			}
		}

		return new Vector2(
			convertToWorldCoord(convertToWorldTileCoord(x), false),
			convertToWorldCoord(convertToWorldTileCoord(y), floor)
		);
	}


	/**
	 * Converts chunk coord + chunk tile coord to world tile coord
	 */
	public static int convertToWorldTileCoord(int chunk, int tile) {
		return chunk * CHUNK_SIZE + tile;
	}


	/**
	 * Converts chunk coord + chunk tile coord to world tile coord
	 */
	public static int convertToWorldTileCoord(float coord) {
		return convertToWorldTileCoord(convertToChunkCoord(coord), convertToTileCoord(coord));
	}


	public static Vector2 convertToWorldCoord(Vector2 coords, boolean floor) {
		return convertToWorldCoord(coords.x, coords.y, floor);
	}


	public static Vector2 convertToWorldCoord(float x, float y, boolean floor) {
		return new Vector2(
			convertToWorldCoord(convertToWorldTileCoord(convertToChunkCoord(x), convertToTileCoord(x)), false),
			convertToWorldCoord(convertToWorldTileCoord(convertToChunkCoord(y), convertToTileCoord(y)), floor)
		);
	}


	/**
	 * Converts a world coordinate into a (chunk)tile coordinate
	 */
	public static int convertToTileCoord(float worldCoord) {
		int worldCoordinateIntegerized = (int) worldCoord;

		if (worldCoordinateIntegerized >= 0) {
			return worldCoordinateIntegerized / TILE_SIZE % CHUNK_SIZE;
		} else {
			return CHUNK_SIZE + worldCoordinateIntegerized / TILE_SIZE % CHUNK_SIZE - 1;
		}
	}


	/**
	 * Deletes a tile at this location
	 *
	 * @param worldX
	 * @param worldY
	 */
	public synchronized Tile deleteTile(float worldX, float worldY, boolean foreGround) {
		int chunkX = convertToChunkCoord(worldX);
		int chunkY = convertToChunkCoord(worldY);
		int tileX = convertToTileCoord(worldX);
		int tileY = convertToTileCoord(worldY);

		try {
			if (getTile(worldX, worldY, foreGround) instanceof EmptyTile) {
				return null;
			}
			Tile tile = getChunkMap().get(chunkX).get(chunkY).getTile(tileX, tileY, foreGround);
			getChunkMap().get(chunkX).get(chunkY).deleteTile(tileX, tileY, foreGround);
			Logger.generalDebug("Deleting tile at (" + convertToWorldTileCoord(chunkX, tileX) + ", " + convertToWorldTileCoord(chunkY, tileY) + "), World coord: (" + worldX + ", " + worldY + ")", LogLevel.TRACE);
			return tile;

		} catch (NullPointerException e) {
			Logger.generalDebug("can't delete a null tile", LogLevel.WARN);
			return null;
		}
	}


	/**
	 * Changes a tile at this location
	 *
	 * @param worldX
	 * @param worldY
	 */
	public synchronized void changeTile(float worldX, float worldY, boolean foreGround, Class<? extends Tile> toChangeTo) {
		int chunkX = convertToChunkCoord(worldX);
		int chunkY = convertToChunkCoord(worldY);
		int tileX = convertToTileCoord(worldX);
		int tileY = convertToTileCoord(worldY);
		try {
			getChunkMap().get(chunkX).get(chunkY).changeTile(tileX, tileY, foreGround, toChangeTo);
		} catch (NullPointerException e) {
			Logger.generalDebug("can't change a null tile", LogLevel.WARN);
		}
	}
	
	
	/**
	 * Changes a tile at this location
	 *
	 * @param worldX
	 * @param worldY
	 */
	public synchronized void changeTile(float worldX, float worldY, boolean foreGround, Tile toChangeTo) {
		int chunkX = convertToChunkCoord(worldX);
		int chunkY = convertToChunkCoord(worldY);
		int tileX = convertToTileCoord(worldX);
		int tileY = convertToTileCoord(worldY);
		try {
			getChunkMap().get(chunkX).get(chunkY).changeTile(tileX, tileY, foreGround, toChangeTo);
		} catch (NullPointerException e) {
			Logger.generalDebug("can't change a null tile", LogLevel.WARN);
		}
	}


	/** Converts a world tile coord to a world coord, in the centre of the tile */
	public static float convertToWorldCoord(int worldTileCoord, boolean floor) {
		return worldTileCoord * Topography.TILE_SIZE + (floor ? 0 : Topography.TILE_SIZE/2);
	}


	/**
	 * Converts a tile coordinate into a chunk coordinate
	 */
	public static int convertToChunkCoord(int tileCoord) {
		if (tileCoord >= 0) {
			return tileCoord / CHUNK_SIZE;
		} else {
			return tileCoord / CHUNK_SIZE - 1;
		}
	}


	/**
	 * Converts a world coordinate into a chunk coordinate
	 */
	public static int convertToChunkCoord(float worldCoord) {
		int worldCoordinateIntegerized = (int) worldCoord;

		if (worldCoordinateIntegerized >= 0) {
			return worldCoordinateIntegerized / TILE_SIZE / CHUNK_SIZE;
		} else {
			return worldCoordinateIntegerized / TILE_SIZE / CHUNK_SIZE - 1;
		}
	}
	
	
	public static void setup() {
		atlas = new Texture(Gdx.files.internal("data/image/textureAtlas.png"));
	}


	/**
	 * Gets a tile given the world coordinates
	 */
	public synchronized Tile getTile(float worldX, float worldY, boolean foreGround) {

		int chunkX = convertToChunkCoord(worldX);
		int chunkY = convertToChunkCoord(worldY);

		int tileX = convertToTileCoord(worldX);
		int tileY = convertToTileCoord(worldY);

		return getChunkMap().get(chunkX).get(chunkY).getTile(tileX, tileY, foreGround);
	}


	/** Overloaded method, see {@link #getTile(float, float)} */
	public synchronized Tile getTile(Vector2 location, boolean foreGround) {
		return getTile(location.x, location.y, foreGround);
	}


	/**
	 * Generates/Loads any missing chunks
	 */
	public void loadOrGenerateNullChunksAccordingToCam(int camX, int camY) {

		int bottomLeftX = convertToChunkCoord((float)(camX - Display.getWidth() / 2));
		int bottomLeftY = convertToChunkCoord((float)(camY - Display.getHeight() / 2));
		int topRightX = bottomLeftX + convertToChunkCoord((float)Display.getWidth());
		int topRightY = bottomLeftY + convertToChunkCoord((float)Display.getHeight());

		for (int chunkX = bottomLeftX - 2; chunkX <= topRightX + 2; chunkX++) {
			for (int chunkY = bottomLeftY - 2; chunkY <= topRightY + 2; chunkY++) {
				if (getChunkMap().get(chunkX) == null || getChunkMap().get(chunkX).get(chunkY) == null) {
					if (ClientServerInterface.isClient() && !ClientServerInterface.isServer()) {
						if (requestedForGeneration.get(chunkX, chunkY) == null || !requestedForGeneration.get(chunkX, chunkY)) {
							ClientServerInterface.SendRequest.sendGenerateChunkRequest(chunkX, chunkY, worldId);
							requestedForGeneration.put(chunkX, chunkY, true);
						}
					} else {
						loadOrGenerateChunk(chunkX, chunkY);
					}
				}
			}
		}
	}


	public boolean loadOrGenerateChunk(int chunkX, int chunkY) {
		//Attempt to load the chunk from disk - If chunk does not exist, it will be generated
		return chunkLoader.load(Domain.getWorld(worldId), chunkX, chunkY);
	}


	public ChunkMap getChunkMap() {
		return chunkMap;
	}


	public Structures getStructures() {
		return structures;
	}
}