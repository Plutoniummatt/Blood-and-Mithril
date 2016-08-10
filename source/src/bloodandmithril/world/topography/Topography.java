package bloodandmithril.world.topography;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Function;
import com.google.common.base.Optional;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.generation.Structures;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.PropPlacementService;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.fluids.FluidStrip;
import bloodandmithril.world.fluids.FluidStripPopulator;
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
	
	/** Unique ID of the {@link World} that this {@link Topography} lives on */
	private final int worldId;

	/** The chunk map of the topography. */
	private final ChunkMap chunkMap;

	/** {@link Structures} that exist on this instance of {@link Topography} */
	private final Structures structures;

	/** The current chunk coordinates that have already been requested for generation */
	private final ConcurrentDualKeyHashMap<Integer, Integer, Boolean> requestedForGeneration = new ConcurrentDualKeyHashMap<>();
	
	private final FluidStripPopulator fluidStripPopulator = new FluidStripPopulator();

	/**
	 * @param generator - The type of generator to use
	 */
	public Topography(int worldId) {
		this.worldId = worldId;
		this.chunkMap = new ChunkMap();
		this.structures = new Structures();
	}


	/** Get the lowest empty tile world coordinates */
	public synchronized final Vector2 getLowestEmptyTileOrPlatformTileWorldCoords(Vector2 worldCoords, boolean floor) throws NoTileFoundException {
		return getLowestEmptyTileOrPlatformTileWorldCoords(worldCoords.x, worldCoords.y, floor);
	}
	
	
	/** Get the tile on the surface from the given location */
	public synchronized Tile getSurfaceTile(float worldX, float worldY, Function<Vector2, Boolean> toExlude) throws NoTileFoundException {
		return getTile(getLowestEmptyTileOrPlatformTileWorldCoordsExludeSpecified(worldX, worldY, false, toExlude).sub(0, TILE_SIZE), true);
	}


	/** Get the lowest empty tile world coordinates */
	public synchronized final Vector2 getLowestEmptyTileOrPlatformTileWorldCoords(float worldX, float worldY, boolean floor) throws NoTileFoundException {
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
	public synchronized final Vector2 getLowestEmptyTileOrPlatformTileWorldCoordsOrHighestNonEmptyNonPlatform(float worldX, float worldY, boolean floor) throws NoTileFoundException {
		if (getTile(worldX, worldY, true) instanceof EmptyTile) {
			while (getTile(worldX, worldY, true) instanceof EmptyTile) {
				worldY = worldY - TILE_SIZE;
			}
			if (worldY != -16.0f) {
				worldY = worldY + TILE_SIZE;
			}
		} else {
			while (!(getTile(worldX, worldY, true).isPassable())) {
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
		float worldX, 
		float worldY, 
		boolean floor, 
		Function<Vector2, Boolean> toExlude
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
	public static final int convertToWorldTileCoord(int chunk, int tile) {
		return chunk * CHUNK_SIZE + tile;
	}


	/**
	 * Converts chunk coord + chunk tile coord to world tile coord
	 */
	public static final int convertToWorldTileCoord(float coord) {
		return convertToWorldTileCoord(convertToChunkCoord(coord), convertToChunkTileCoord(coord));
	}


	public static final Vector2 convertToWorldCoord(Vector2 coords, boolean floor) throws NoTileFoundException {
		if (coords == null) {
			throw new NoTileFoundException(null, null);
		}
		return convertToWorldCoord(coords.x, coords.y, floor);
	}


	public static final Vector2 convertToWorldCoord(float x, float y, boolean floor) {
		return new Vector2(
			convertToWorldCoord(convertToWorldTileCoord(convertToChunkCoord(x), convertToChunkTileCoord(x)), false),
			convertToWorldCoord(convertToWorldTileCoord(convertToChunkCoord(y), convertToChunkTileCoord(y)), floor)
		);
	}


	/**
	 * Converts a world coordinate into a (chunk)tile coordinate
	 */
	public static final int convertToChunkTileCoord(float worldCoord) {
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


	/**
	 * Deletes a tile at this location
	 *
	 * @param worldX
	 * @param worldY
	 * @return The tile which was deleted.
	 */
	public synchronized final Tile deleteTile(float worldX, float worldY, boolean foreGround, boolean forceRemove) {
		int chunkX = convertToChunkCoord(worldX);
		int chunkY = convertToChunkCoord(worldY);
		int tileX = convertToChunkTileCoord(worldX);
		int tileY = convertToChunkTileCoord(worldY);

		if (!forceRemove && preventedByProps(worldX, worldY)) {
			return null;
		}

		try {
			if (getTile(worldX, worldY, foreGround) instanceof EmptyTile) {
				return null;
			}
			Tile tile = getChunkMap().get(chunkX).get(chunkY).getTile(tileX, tileY, foreGround);
			getChunkMap().get(chunkX).get(chunkY).deleteTile(tileX, tileY, foreGround);
			Logger.generalDebug("Deleting tile at (" + convertToWorldTileCoord(chunkX, tileX) + ", " + convertToWorldTileCoord(chunkY, tileY) + "), World coord: (" + worldX + ", " + worldY + ")", LogLevel.TRACE);
			if (!forceRemove) {
				removeProps(worldX, worldY);
			}
			if(
				Domain.getWorld(worldId).fluids().getFluidStrip(convertToWorldTileCoord(worldX) - 1, convertToWorldTileCoord(worldY)).isPresent() ||
				Domain.getWorld(worldId).fluids().getFluidStrip(convertToWorldTileCoord(worldX) + 1, convertToWorldTileCoord(worldY)).isPresent()
			) {
				fluidStripPopulator.createFluidStrip(Domain.getWorld(worldId), convertToWorldTileCoord(worldX), convertToWorldTileCoord(worldY), 0f);
			} else {
				fluidStripPopulator.createFluidStripIfBase(Domain.getWorld(worldId), convertToWorldTileCoord(worldX), convertToWorldTileCoord(worldY), 0f);
			}
			return tile;

		} catch (NoTileFoundException e) {
			Logger.generalDebug("can't delete a null tile", LogLevel.WARN);
			return null;
		}
	}


	private final void removeProps(float worldX, float worldY) {
		Tile deletedTile = deleteTile(worldX, worldY, true, true);

		World world = Domain.getWorld(worldId);
		world.getPositionalIndexMap().getNearbyEntityIds(Prop.class, worldX, worldY).forEach(id -> {
			Prop prop = world.props().getProp(id);
			
			boolean canPlace = false;
			try {
				canPlace = Wiring.injector().getInstance(PropPlacementService.class).canPlaceAtCurrentPosition(prop);
			} catch (NoTileFoundException e) {}
			
			if (!canPlace && !prop.preventsMining) {
				world.props().removeProp(prop.id);
			}
		});

		if (deletedTile != null) {
			changeTile(worldX, worldY, true, deletedTile);
		}
	}


	private final boolean preventedByProps(float worldX, float worldY) {
		Tile deletedTile = deleteTile(worldX, worldY, true, true);

		final MutableBoolean b = new MutableBoolean();
		b.setValue(false);

		World world = Domain.getWorld(worldId);
		world.getPositionalIndexMap().getNearbyEntityIds(Prop.class, worldX, worldY).forEach(id -> {
			Prop prop = world.props().getProp(id);
			
			boolean canPlace = false;
			try {
				canPlace = Wiring.injector().getInstance(PropPlacementService.class).canPlaceAtCurrentPosition(prop);
			} catch (NoTileFoundException e) {}
			
			if (!canPlace && prop.preventsMining) {
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
	public synchronized final void changeTile(float worldX, float worldY, boolean foreGround, Class<? extends Tile> toChangeTo) {
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
	public synchronized final void changeTile(float worldX, float worldY, boolean foreGround, Tile toChangeTo) {
		if (toChangeTo instanceof EmptyTile) {
			return;
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
		
		Optional<FluidStrip> stripOn = Domain.getWorld(worldId).fluids().getFluidStrip(convertToWorldTileCoord(worldX), convertToWorldTileCoord(worldY));
		if(stripOn.isPresent()) {
				if(!fluidStripPopulator.splitFluidStrip(Domain.getWorld(worldId), stripOn.get(), convertToWorldTileCoord(worldX))) {
					Optional<FluidStrip> stripAbove = Domain.getWorld(worldId).fluids().getFluidStrip(convertToWorldTileCoord(worldX), convertToWorldTileCoord(worldY) + 1);
					if(stripAbove.isPresent()) {
						stripAbove.get().addVolume(stripOn.get().getVolume());
					} else {
						fluidStripPopulator.createFluidStrip(Domain.getWorld(worldId), convertToWorldTileCoord(worldX), convertToWorldTileCoord(worldY) + 1, stripOn.get().getVolume());
					}
					Domain.getWorld(worldId).fluids().removeFluidStrip(stripOn.get().id);
				}
			} else {
				fluidStripPopulator.createFluidStrip(Domain.getWorld(worldId), convertToWorldTileCoord(worldX) - 1, convertToWorldTileCoord(worldY), 0f);
				fluidStripPopulator.createFluidStrip(Domain.getWorld(worldId), convertToWorldTileCoord(worldX) + 1, convertToWorldTileCoord(worldY), 0f);
				fluidStripPopulator.createFluidStrip(Domain.getWorld(worldId), convertToWorldTileCoord(worldX), convertToWorldTileCoord(worldY) + 1, 0f);
			}
	}


	/** Converts a world tile coord to a world coord, in the centre of the tile */
	public static final float convertToWorldCoord(int worldTileCoord, boolean floor) {
		return worldTileCoord * Topography.TILE_SIZE + (floor ? 0 : Topography.TILE_SIZE/2);
	}


	/**
	 * Converts a tile coordinate into a chunk coordinate
	 */
	public static final int convertToChunkCoord(int tileCoord) {
		if (tileCoord >= 0) {
			return tileCoord / CHUNK_SIZE;
		} else {
			return (tileCoord + 1) / CHUNK_SIZE - 1;
		}
	}


	/**
	 * Converts a world coordinate into a chunk coordinate
	 */
	public static final int convertToChunkCoord(float worldCoord) {
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


	public final boolean hasTile(float worldX, float worldY, boolean foreGround) {
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
	public synchronized final Tile getTile(float worldX, float worldY, boolean foreGround) throws NoTileFoundException {
		int chunkX = convertToChunkCoord(worldX);
		int chunkY = convertToChunkCoord(worldY);
		
		int tileX = convertToChunkTileCoord(worldX);
		int tileY = convertToChunkTileCoord(worldY);
		
		try {
			return getChunkMap().get(chunkX).get(chunkY).getTile(tileX, tileY, foreGround);
		} catch (NullPointerException e) {
			throw new NoTileFoundException(chunkX, chunkY);
		}
	}


	/**
	 * Gets a tile given the world tile coordinates
	 */
	public synchronized final Tile getTile(int tileX, int tileY, boolean foreGround) throws NoTileFoundException {
		int chunkX = convertToChunkCoord(convertToWorldCoord(tileX, false));
		int chunkY = convertToChunkCoord(convertToWorldCoord(tileY, false));

		int chunkTileX = convertToChunkTileCoord(convertToWorldCoord(tileX, false));
		int chunkTileY = convertToChunkTileCoord(convertToWorldCoord(tileY, false));

		try {
			return getChunkMap().get(chunkX).get(chunkY).getTile(chunkTileX, chunkTileY, foreGround);
		} catch (NullPointerException e) {
			throw new NoTileFoundException(chunkX, chunkY);
		}
	}


	/** Overloaded method, see {@link #getTile(float, float)} */
	public synchronized final Tile getTile(Vector2 location, boolean foreGround) throws NoTileFoundException {
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
		
		public NoTileFoundException(Integer chunkX, Integer chunkY) {
			this.chunkX = chunkX;
			this.chunkY = chunkY;
		}
	}
}