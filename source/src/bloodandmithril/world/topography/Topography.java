package bloodandmithril.world.topography;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Function;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.Structures;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;
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

	/** The texture atlas containing all textures for tiles */
	public static Texture TILE_TEXTURE_ATLAS;

	/** The texture coordinate increment representing one tile in the texture atlas. (1/128) */
	public static final float TEXTURE_COORDINATE_QUANTIZATION = 0.0078125f;

	/** The chunk map of the topography. */
	private final ChunkMap chunkMap;

	/** {@link Structures} that exist on this instance of {@link Topography} */
	private final Structures structures;

	/** The current chunk coordinates that have already been requested for generation */
	private final ConcurrentDualKeyHashMap<Integer, Integer, Boolean> requestedForGeneration = new ConcurrentDualKeyHashMap<>();

	/**
	 * @param generator - The type of generator to use
	 */
	public Topography() {
		this.chunkMap = new ChunkMap();
		this.structures = new Structures();
	}


	/** Get the lowest empty tile world coordinates */
	public synchronized final Vector2 getLowestEmptyTileOrPlatformTileWorldCoords(final Vector2 worldCoords, final boolean floor) throws NoTileFoundException {
		return getLowestEmptyTileOrPlatformTileWorldCoords(worldCoords.x, worldCoords.y, floor);
	}


	/** Get the tile on the surface from the given location */
	public synchronized Tile getSurfaceTile(final float worldX, final float worldY, final Function<Vector2, Boolean> toExlude) throws NoTileFoundException {
		return getTile(getLowestEmptyTileOrPlatformTileWorldCoordsExludeSpecified(worldX, worldY, false, toExlude).sub(0, TILE_SIZE), true);
	}


	/** Get the lowest empty tile world coordinates */
	public synchronized final Vector2 getLowestEmptyTileOrPlatformTileWorldCoords(final float worldX, float worldY, final boolean floor) throws NoTileFoundException {
		if (getTile(worldX, worldY, true) instanceof EmptyTile) {
			while (getTile(worldX, worldY, true) instanceof EmptyTile) {
				worldY = worldY - TILE_SIZE;
			}
			if (worldY != -16.0f) {
				worldY = worldY + TILE_SIZE;
		 	}
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


	/** Get the lowest empty tile world coordinates, or the highest non empty non platform (if starting position is non-empty) */
	public synchronized final Vector2 getLowestEmptyTileOrPlatformTileWorldCoordsOrHighestNonEmptyNonPlatform(final float worldX, float worldY, final boolean floor) throws NoTileFoundException {
		if (getTile(worldX, worldY, true) instanceof EmptyTile) {
			while (getTile(worldX, worldY, true) instanceof EmptyTile) {
				worldY = worldY - TILE_SIZE;
			}
			if (worldY != -16.0f) {
				worldY = worldY + TILE_SIZE;
			}
		} else {
			while (!getTile(worldX, worldY, true).isPassable()) {
				worldY = worldY + TILE_SIZE;
			}
		}

		return new Vector2(
			convertToWorldCoord(convertToWorldTileCoord(worldX), false),
			convertToWorldCoord(convertToWorldTileCoord(worldY), floor)
		);
	}


	/** Get the lowest empty tile world coordinates */
	public synchronized final Vector2 getLowestEmptyTileOrPlatformTileWorldCoordsExludeSpecified(
		final float worldX,
		float worldY,
		final boolean floor,
		final Function<Vector2, Boolean> toExlude
	) throws NoTileFoundException {
		if (getTile(worldX, worldY, true) instanceof EmptyTile || toExlude.apply(new Vector2(worldX, worldY))) {
			while (getTile(worldX, worldY, true) instanceof EmptyTile || toExlude.apply(new Vector2(worldX, worldY))) {
				worldY = worldY - TILE_SIZE;
			}
			if (worldY != -16.0f) {
				worldY = worldY + TILE_SIZE;
			}
		} else {
			while (!(getTile(worldX, worldY, true) instanceof EmptyTile || !toExlude.apply(new Vector2(worldX, worldY)))) {
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
			throw new NoTileFoundException(null, null);
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
		int worldCoordinateIntegerized = (int) worldCoord;

		if (worldCoord < 0f && worldCoord > -1f) {
			worldCoordinateIntegerized -= 1;
		}

		if (worldCoordinateIntegerized < 0 && worldCoordinateIntegerized % 16 == 0) {
			if (worldCoordinateIntegerized <= worldCoord) {
				worldCoordinateIntegerized += 1;
			}
		}

		if (worldCoordinateIntegerized >= 0) {
			return worldCoordinateIntegerized / TILE_SIZE % CHUNK_SIZE;
		} else {
			return CHUNK_SIZE + worldCoordinateIntegerized / TILE_SIZE % CHUNK_SIZE - 1;
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
		int worldCoordinateIntegerized = (int) worldCoord;

		if (worldCoord < 0f && worldCoord > -1f) {
			worldCoordinateIntegerized -= 1;
		}

		if (worldCoordinateIntegerized < 0 && worldCoordinateIntegerized % CHUNK_SIZE == 0) {
			if (worldCoordinateIntegerized <= worldCoord) {
				worldCoordinateIntegerized += 1;
			}
		}

		if (worldCoordinateIntegerized >= 0) {
			return worldCoordinateIntegerized / TILE_SIZE / CHUNK_SIZE;
		} else {
			return worldCoordinateIntegerized / TILE_SIZE / CHUNK_SIZE - 1;
		}
	}


	public static final void setup() {
		TILE_TEXTURE_ATLAS = new Texture(Gdx.files.internal("data/image/textureAtlas.png"));
	}


	public synchronized final boolean hasTile(final float worldX, final float worldY, final boolean foreGround) {
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
		final int chunkX = convertToChunkCoord(worldX);
		final int chunkY = convertToChunkCoord(worldY);

		final int tileX = convertToChunkTileCoord(worldX);
		final int tileY = convertToChunkTileCoord(worldY);

		try {
			return getChunkMap().get(chunkX).get(chunkY).getTile(tileX, tileY, foreGround);
		} catch (final NullPointerException e) {
			throw new NoTileFoundException(chunkX, chunkY);
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
			throw new NoTileFoundException(chunkX, chunkY);
		}
	}


	/** Overloaded method, see {@link #getTile(float, float)} */
	public synchronized final Tile getTile(final Vector2 location, final boolean foreGround) throws NoTileFoundException {
		return getTile(location.x, location.y, foreGround);
	}


	public final ChunkMap getChunkMap() {
		return chunkMap;
	}


	public final Structures getStructures() {
		return structures;
	}


	public ConcurrentDualKeyHashMap<Integer, Integer, Boolean> getRequestedForGeneration() {
		return requestedForGeneration;
	}


	public static final class NoTileFoundException extends Exception {
		private static final long serialVersionUID = 5955361949995345496L;
		public final Integer chunkX;
		public final Integer chunkY;

		public NoTileFoundException(final Integer chunkX, final Integer chunkY) {
			this.chunkX = chunkX;
			this.chunkY = chunkY;
		}
	}
}