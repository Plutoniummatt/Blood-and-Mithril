package bloodandmithril.world.topography;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.lwjgl.opengl.Display;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.generation.Structures;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Operator;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents the topography of the gameworld - Chunks, Tiles, and objects that
 * exist within it etc.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
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
	public static final float textureCoordinateQuantization = 0.015625f;

	/** The chunk map of the topography. */
	private final ChunkMap chunkMap;

	/** {@link Structures} that exist on this instance of {@link Topography} */
	private final Structures structures;

	/** The chunk loader. */
	private static final ChunkLoader chunkLoader = new ChunkLoader();

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
	public static synchronized void addTask(Task task) {
		topographyTasks.add(task);
	}


	/** Executes any tasks queued in {@link #topographyTasks} by other threads */
	public static synchronized void executeBackLog() {
		while (!topographyTasks.isEmpty()) {
			topographyTasks.poll().execute();
		}
	}


	/**
	 * Renders the background
	 */
	public void renderBackGround(int camX, int camY, ShaderProgram shader, Operator<ShaderProgram> uniformSettings) {
		int bottomLeftX 	= (camX - Display.getWidth() / 2) / (CHUNK_SIZE * TILE_SIZE);
		int bottomLeftY 	= (camY - Display.getHeight() / 2) / (CHUNK_SIZE * TILE_SIZE);
		int topRightX 		= bottomLeftX + Display.getWidth() / (CHUNK_SIZE * TILE_SIZE);
		int topRightY		= bottomLeftY + Display.getHeight() / (CHUNK_SIZE * TILE_SIZE);

		Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Topography.atlas.bind();
		for (int x = bottomLeftX - 2; x <= topRightX + 2; x++) {
			for (int y = bottomLeftY - 2; y <= topRightY + 2; y++) {
				if (getChunkMap().get(x) != null && getChunkMap().get(x).get(y) != null) {
					getChunkMap().get(x).get(y).checkMesh();
					getChunkMap().get(x).get(y).render(false, BloodAndMithrilClient.cam, shader, uniformSettings);
				}
			}
		}
	}


	/**
	 * Renders the foreground
	 */
	public void renderForeGround(int camX, int camY, ShaderProgram shader, Operator<ShaderProgram> uniformSettings) {
		int bottomLeftX 	= (camX - Display.getWidth() / 2) / (CHUNK_SIZE * TILE_SIZE);
		int bottomLeftY 	= (camY - Display.getHeight() / 2) / (CHUNK_SIZE * TILE_SIZE);
		int topRightX 		= bottomLeftX + Display.getWidth() / (CHUNK_SIZE * TILE_SIZE);
		int topRightY		= bottomLeftY + Display.getHeight() / (CHUNK_SIZE * TILE_SIZE);

		Topography.atlas.bind();
		for (int x = bottomLeftX - 2; x <= topRightX + 2; x++) {
			for (int y = bottomLeftY - 2; y <= topRightY + 2; y++) {
				if (getChunkMap().get(x) != null && getChunkMap().get(x).get(y) != null) {
					getChunkMap().get(x).get(y).checkMesh();
					getChunkMap().get(x).get(y).render(true, BloodAndMithrilClient.cam, shader, uniformSettings);
				}
			}
		}
	}


	/** Get the lowest empty tile world coordinates */
	public synchronized Vector2 getLowestEmptyTileOrPlatformTileWorldCoords(Vector2 worldCoords, boolean floor) throws NoTileFoundException {
		return getLowestEmptyTileOrPlatformTileWorldCoords(worldCoords.x, worldCoords.y, floor);
	}


	/** Get the lowest empty tile world coordinates */
	public synchronized Vector2 getLowestEmptyTileOrPlatformTileWorldCoords(float worldX, float worldY, boolean floor) throws NoTileFoundException {
		if (getTile(worldX, worldY, true) instanceof EmptyTile) {
			while (getTile(worldX, worldY, true) instanceof EmptyTile) {
				worldY = worldY - TILE_SIZE;
			}
			worldY = worldY + TILE_SIZE;
		} else {
			while (!(getTile(worldX, worldY, true) instanceof EmptyTile)) {
				worldY = worldY + TILE_SIZE;
			}
		}

		return new Vector2(
			convertToWorldCoord(convertToWorldTileCoord(worldX), false),
			convertToWorldCoord(convertToWorldTileCoord(worldY), floor)
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
		return convertToWorldTileCoord(convertToChunkCoord(coord), convertToChunkTileCoord(coord));
	}


	public static Vector2 convertToWorldCoord(Vector2 coords, boolean floor) {
		return convertToWorldCoord(coords.x, coords.y, floor);
	}


	public static Vector2 convertToWorldCoord(float x, float y, boolean floor) {
		return new Vector2(
			convertToWorldCoord(convertToWorldTileCoord(convertToChunkCoord(x), convertToChunkTileCoord(x)), false),
			convertToWorldCoord(convertToWorldTileCoord(convertToChunkCoord(y), convertToChunkTileCoord(y)), floor)
		);
	}


	/**
	 * Converts a world coordinate into a (chunk)tile coordinate
	 */
	public static int convertToChunkTileCoord(float worldCoord) {
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
		int tileX = convertToChunkTileCoord(worldX);
		int tileY = convertToChunkTileCoord(worldY);

		try {
			if (getTile(worldX, worldY, foreGround) instanceof EmptyTile) {
				return null;
			}
			Tile tile = getChunkMap().get(chunkX).get(chunkY).getTile(tileX, tileY, foreGround);
			getChunkMap().get(chunkX).get(chunkY).deleteTile(tileX, tileY, foreGround);
			Logger.generalDebug("Deleting tile at (" + convertToWorldTileCoord(chunkX, tileX) + ", " + convertToWorldTileCoord(chunkY, tileY) + "), World coord: (" + worldX + ", " + worldY + ")", LogLevel.TRACE);
			return tile;

		} catch (NoTileFoundException e) {
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
		try {
			changeTile(worldX, worldY, foreGround, toChangeTo.newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Changes a tile at this location
	 *
	 * @param worldX
	 * @param worldY
	 */
	public synchronized void changeTile(float worldX, float worldY, boolean foreGround, Tile toChangeTo) {
		if (toChangeTo instanceof EmptyTile) {
			throw new IllegalStateException("Can't change tile to empty tile");
		}

		int chunkX = convertToChunkCoord(worldX);
		int chunkY = convertToChunkCoord(worldY);
		int tileX = convertToChunkTileCoord(worldX);
		int tileY = convertToChunkTileCoord(worldY);

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


	public boolean hasTile(float worldX, float worldY, boolean foreGround) {
		int chunkX = convertToChunkCoord(worldX);
		int chunkY = convertToChunkCoord(worldY);

		int tileX = convertToChunkTileCoord(worldX);
		int tileY = convertToChunkTileCoord(worldY);

		if (getChunkMap().get(chunkX) == null) {
			return false;
		}

		if (getChunkMap().get(chunkX).get(chunkY) == null) {
			return false;
		}

		return getChunkMap().get(chunkX).get(chunkY).getTile(tileX, tileY, foreGround) != null;
	}


	/**
	 * Gets a tile given the world coordinates
	 */
	public synchronized Tile getTile(float worldX, float worldY, boolean foreGround) throws NoTileFoundException {
		try {
			int chunkX = convertToChunkCoord(worldX);
			int chunkY = convertToChunkCoord(worldY);

			int tileX = convertToChunkTileCoord(worldX);
			int tileY = convertToChunkTileCoord(worldY);

			return getChunkMap().get(chunkX).get(chunkY).getTile(tileX, tileY, foreGround);
		} catch (NullPointerException e) {
			throw new NoTileFoundException();
		}
	}


	/**
	 * Gets a tile given the world tile coordinates
	 */
	public synchronized Tile getTile(int tileX, int tileY, boolean foreGround) throws NoTileFoundException {
		int chunkX = convertToChunkCoord(convertToWorldCoord(tileX, false));
		int chunkY = convertToChunkCoord(convertToWorldCoord(tileY, false));

		int chunkTileX = convertToChunkTileCoord(convertToWorldCoord(tileX, false));
		int chunkTileY = convertToChunkTileCoord(convertToWorldCoord(tileY, false));

		try {
			return getChunkMap().get(chunkX).get(chunkY).getTile(chunkTileX, chunkTileY, foreGround);
		} catch (NullPointerException e) {
			throw new NoTileFoundException();
		}
	}


	/** Overloaded method, see {@link #getTile(float, float)} */
	public synchronized Tile getTile(Vector2 location, boolean foreGround) throws NoTileFoundException {
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


	public static class NoTileFoundException extends Exception {
		private static final long serialVersionUID = 5955361949995345496L;
	}
}