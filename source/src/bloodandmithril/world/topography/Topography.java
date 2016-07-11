package bloodandmithril.world.topography;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.Structures;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * Represents the topography of the gameworld - Chunks, Tiles, and objects that
 * exist within it etc.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public final class Topography {


	/** The size of a single tile, in pixels (single dimension) */
	public static final int TILE_SIZE = 16;

	/** The size of a chunk, in number of tiles (single dimension) */
	public static final int CHUNK_SIZE = 20;

	/** The texture coordinate increment representing one tile in the texture atlas. (1/128) */
	public static final float TEXTURE_COORDINATE_QUANTIZATION = 0.015625f;

	/** Unique ID of the {@link World} that this {@link Topography} lives on */
	private final int worldId;

	/** The chunk map of the topography. */
	private final ChunkMap chunkMap;

	/** {@link Structures} that exist on this instance of {@link Topography} */
	private final Structures structures;

	/** Any non-main thread topography tasks queued here */
	private static BlockingQueue<Task> topographyTasks = new ArrayBlockingQueue<Task>(500000);

	/** The current chunk coordinates that have already been requested for generation */
	private final ConcurrentDualKeyHashMap<Integer, Integer, Boolean> requestedForGeneration = new ConcurrentDualKeyHashMap<>();

	/**
	 * @param generator - The type of generator to use
	 */
	public Topography(final int worldId) {
		this.worldId = worldId;
		this.chunkMap = new ChunkMap();
		this.structures = new Structures();
	}


	/** Adds a task to be processed */
	public static synchronized final void addTask(final Task task) {
		topographyTasks.add(task);
	}


	/** Executes any tasks queued in {@link #topographyTasks} by other threads */
	public static synchronized final void executeBackLog() {
		while (!topographyTasks.isEmpty()) {
			topographyTasks.poll().execute();
		}
	}


	/** Get the lowest empty tile world coordinates */
	public synchronized final Vector2 getLowestEmptyTileOrPlatformTileWorldCoords(final Vector2 worldCoords, final boolean floor) throws NoTileFoundException {
		return getLowestEmptyTileOrPlatformTileWorldCoords(worldCoords.x, worldCoords.y, floor);
	}


	/** Get the lowest empty tile world coordinates */
	public synchronized final Vector2 getLowestEmptyTileOrPlatformTileWorldCoords(final float worldX, float worldY, final boolean floor) throws NoTileFoundException {
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
	public static final int convertToWorldTileCoord(final int chunk, final int tile) {
		return chunk * CHUNK_SIZE + tile;
	}


	/**
	 * Converts chunk coord + chunk tile coord to world tile coord
	 */
	public static final int convertToWorldTileCoord(final float coord) {
		return convertToWorldTileCoord(convertToChunkCoord(coord), convertToChunkTileCoord(coord));
	}


	public static final Vector2 convertToWorldCoord(final Vector2 coords, final boolean floor) throws NoTileFoundException {
		if (coords == null) {
			throw new NoTileFoundException();
		}
		return convertToWorldCoord(coords.x, coords.y, floor);
	}


	public static final Vector2 convertToWorldCoord(final float x, final float y, final boolean floor) {
		return new Vector2(
			convertToWorldCoord(convertToWorldTileCoord(convertToChunkCoord(x), convertToChunkTileCoord(x)), false),
			convertToWorldCoord(convertToWorldTileCoord(convertToChunkCoord(y), convertToChunkTileCoord(y)), floor)
		);
	}


	/**
	 * Converts a world coordinate into a (chunk)tile coordinate
	 */
	public static final int convertToChunkTileCoord(final float worldCoord) {
		final int worldCoordinateIntegerized = (int) worldCoord;

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
	public synchronized final Tile deleteTile(final float worldX, final float worldY, final boolean foreGround, final boolean forceRemove) {
		final int chunkX = convertToChunkCoord(worldX);
		final int chunkY = convertToChunkCoord(worldY);
		final int tileX = convertToChunkTileCoord(worldX);
		final int tileY = convertToChunkTileCoord(worldY);

		if (!forceRemove && preventedByProps(worldX, worldY)) {
			return null;
		}

		try {
			if (getTile(worldX, worldY, foreGround) instanceof EmptyTile) {
				return null;
			}
			final Tile tile = getChunkMap().get(chunkX).get(chunkY).getTile(tileX, tileY, foreGround);
			getChunkMap().get(chunkX).get(chunkY).deleteTile(tileX, tileY, foreGround);
			Logger.generalDebug("Deleting tile at (" + convertToWorldTileCoord(chunkX, tileX) + ", " + convertToWorldTileCoord(chunkY, tileY) + "), World coord: (" + worldX + ", " + worldY + ")", LogLevel.TRACE);
			if (!forceRemove) {
				removeProps(worldX, worldY);
			}
			return tile;

		} catch (final NoTileFoundException e) {
			Logger.generalDebug("can't delete a null tile", LogLevel.WARN);
			return null;
		}
	}


	private final void removeProps(final float worldX, final float worldY) {
		final Tile deletedTile = deleteTile(worldX, worldY, true, true);

		final World world = Domain.getWorld(worldId);
		world.getPositionalIndexMap().getNearbyEntityIds(Prop.class, worldX, worldY).forEach(id -> {
			final Prop prop = world.props().getProp(id);
			if (!prop.canPlaceAtCurrentPosition() && !prop.preventsMining) {
				world.props().removeProp(prop.id);
			}
		});

		if (deletedTile != null) {
			changeTile(worldX, worldY, true, deletedTile);
		}
	}


	private final boolean preventedByProps(final float worldX, final float worldY) {
		final Tile deletedTile = deleteTile(worldX, worldY, true, true);

		final MutableBoolean b = new MutableBoolean();
		b.setValue(false);

		final World world = Domain.getWorld(worldId);
		world.getPositionalIndexMap().getNearbyEntityIds(Prop.class, worldX, worldY).forEach(id -> {
			final Prop prop = world.props().getProp(id);
			if (!prop.canPlaceAtCurrentPosition() && prop.preventsMining) {
				b.setValue(true);
			}
		});

		if (deletedTile != null) {
			changeTile(worldX, worldY, true, deletedTile);
		}

		return b.booleanValue();
	}


	/**
	 * Changes a tile at this location
	 *
	 * @param worldX
	 * @param worldY
	 */
	public synchronized final void changeTile(final float worldX, final float worldY, final boolean foreGround, final Class<? extends Tile> toChangeTo) {
		try {
			changeTile(worldX, worldY, foreGround, toChangeTo.newInstance());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Changes a tile at this location
	 *
	 * @param worldX
	 * @param worldY
	 */
	public synchronized final void changeTile(final float worldX, final float worldY, final boolean foreGround, final Tile toChangeTo) {
		if (toChangeTo instanceof EmptyTile) {
			throw new IllegalStateException("Can't change tile to empty tile");
		}

		final int chunkX = convertToChunkCoord(worldX);
		final int chunkY = convertToChunkCoord(worldY);
		final int tileX = convertToChunkTileCoord(worldX);
		final int tileY = convertToChunkTileCoord(worldY);

		try {
			getChunkMap().get(chunkX).get(chunkY).changeTile(tileX, tileY, foreGround, toChangeTo);
		} catch (final NullPointerException e) {
			Logger.generalDebug("can't change a null tile", LogLevel.WARN);
		}
	}


	/** Converts a world tile coord to a world coord, in the centre of the tile */
	public static final float convertToWorldCoord(final int worldTileCoord, final boolean floor) {
		return worldTileCoord * Topography.TILE_SIZE + (floor ? 0 : Topography.TILE_SIZE/2);
	}


	/**
	 * Converts a tile coordinate into a chunk coordinate
	 */
	public static final int convertToChunkCoord(final int tileCoord) {
		if (tileCoord >= 0) {
			return tileCoord / CHUNK_SIZE;
		} else {
			return (tileCoord + 1) / CHUNK_SIZE - 1;
		}
	}


	/**
	 * Converts a world coordinate into a chunk coordinate
	 */
	public static final int convertToChunkCoord(final float worldCoord) {
		final int worldCoordinateIntegerized = (int) worldCoord;

		if (worldCoordinateIntegerized >= 0) {
			return worldCoordinateIntegerized / TILE_SIZE / CHUNK_SIZE;
		} else {
			return worldCoordinateIntegerized / TILE_SIZE / CHUNK_SIZE - 1;
		}
	}


	public final boolean hasTile(final float worldX, final float worldY, final boolean foreGround) {
		final int chunkX = convertToChunkCoord(worldX);
		final int chunkY = convertToChunkCoord(worldY);

		final int tileX = convertToChunkTileCoord(worldX);
		final int tileY = convertToChunkTileCoord(worldY);

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
	public synchronized final Tile getTile(final float worldX, final float worldY, final boolean foreGround) throws NoTileFoundException {
		try {
			final int chunkX = convertToChunkCoord(worldX);
			final int chunkY = convertToChunkCoord(worldY);

			final int tileX = convertToChunkTileCoord(worldX);
			final int tileY = convertToChunkTileCoord(worldY);

			return getChunkMap().get(chunkX).get(chunkY).getTile(tileX, tileY, foreGround);
		} catch (final NullPointerException e) {
			throw new NoTileFoundException();
		}
	}


	/**
	 * Gets a tile given the world tile coordinates
	 */
	public synchronized final Tile getTile(final int tileX, final int tileY, final boolean foreGround) throws NoTileFoundException {
		final int chunkX = convertToChunkCoord(convertToWorldCoord(tileX, false));
		final int chunkY = convertToChunkCoord(convertToWorldCoord(tileY, false));

		final int chunkTileX = convertToChunkTileCoord(convertToWorldCoord(tileX, false));
		final int chunkTileY = convertToChunkTileCoord(convertToWorldCoord(tileY, false));

		try {
			return getChunkMap().get(chunkX).get(chunkY).getTile(chunkTileX, chunkTileY, foreGround);
		} catch (final NullPointerException e) {
			throw new NoTileFoundException();
		}
	}


	/** Overloaded method, see {@link #getTile(float, float)} */
	public synchronized final Tile getTile(final Vector2 location, final boolean foreGround) throws NoTileFoundException {
		return getTile(location.x, location.y, foreGround);
	}


	public boolean isChunkPendingGeneration(final int chunkX, final int chunkY) {
		return !(requestedForGeneration.get(chunkX, chunkY) == null || !requestedForGeneration.get(chunkX, chunkY));
	}


	public boolean setChunkPendingGeneration(final int chunkX, final int chunkY) {
		return requestedForGeneration.put(chunkX, chunkY, true);
	}


	public final ChunkMap getChunkMap() {
		return chunkMap;
	}


	public final Structures getStructures() {
		return structures;
	}


	public static final class NoTileFoundException extends Exception {
		private static final long serialVersionUID = 5955361949995345496L;
	}
}