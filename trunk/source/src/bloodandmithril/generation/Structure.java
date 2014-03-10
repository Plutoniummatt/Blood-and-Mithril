package bloodandmithril.generation;

import java.io.Serializable;
import java.util.List;

import bloodandmithril.generation.component.Component;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.google.common.collect.Lists;

/**
 * A {@link Structure} which can later be used to generate a {@link Chunk}
 *
 * @author Sam, Matt
 */
public abstract class Structure implements Serializable {
	private static final long serialVersionUID = -5890196858721145717L;
	
	/** Unique ID of the world that holds the {@link Topography} in which this {@link Structure} exists */
	protected final int worldId;

	/** The key used by this {@link Structure} in the {@link Structures} */
	private int structureKey;

	/** The number of chunks this structure has left to generate on it */
	private int chunksLeftToBeGenerated = -1;

	/** {@link Component}s on this {@link Structure} */
	private final List<Component> components = Lists.newArrayList();

	/**
	 * @return whether this {@link Structure} has finished generating.
	 *
	 * If it has, the structure can be deleted from the structure map.
	 */
	public boolean allChunksGenerated() {
		return getChunksLeftToBeGenerated() == 0;
	}
	
	
	/**
	 * Constructor
	 */
	protected Structure(int worldId) {
		this.worldId = worldId;
	}


	/**
	 * Calculates available unoccupied space for the structure.
	 * Generates the structure.
	 * Adds this Structure to the key maps.
	 * Calculates chunks to generate.
	 *
	 * @param startingChunkX - the chunk coordinates to start generating the structure from
	 * @param startingChunkY - the chunk coordinates to start generating the structure from
	 * @param generatingToRight - true if generating to the right
	 */
	public void generate(int startingChunkX, int startingChunkY, boolean generatingToRight) {
		findSpaceAndAddToMap(startingChunkX, startingChunkY, generatingToRight);
		internalGenerate(generatingToRight);
		calculateChunksToGenerate();
	}


	/**
	 * Makes the structure.
	 *
	 * @param startingChunkX - the chunk coordinates to start generating the structure from
	 * @param startingChunkY - the chunk coordinates to start generating the structure from
	 * @param generatingToRight - true if generating to the right
	 */
	protected abstract void findSpaceAndAddToMap(int startingChunkX, int startingChunkY, boolean generatingToRight);


	/**
	 * Adds a structure to the {@link Structures}.
	 *
	 * @return - the key used by this structure on the {@link Structures}.
	 */
	protected abstract int addToStructureMap();


	/**
	 * Generate the {@link Structure}.
	 */
	protected abstract void internalGenerate(boolean generatingToRight);


	/**
	 * Calculate how many chunks the Structure includes. The structure will be deleted when this many have been generated.
	 */
	protected abstract void calculateChunksToGenerate();


	/**
	 * Get a foreground tile from the {@link Structure}.
	 */
	protected abstract Tile internalGetForegroundTile(int worldTileX, int worldTileY);


	/**
	 * Get the background tile from the {@link Structure}.
	 */
	protected abstract Tile internalGetBackgroundTile(int worldTileX, int worldTileY);


	/**
	 * Get a foreground tile from the {@link Structure}.
	 *
	 * @param worldTileX - world tile coordinates
	 * @param worldTileY - world tile coordinates
	 * @return - the {@link Tile} the {@link Structure} says it is.
	 */
	public Tile getForegroundTile(int worldTileX, int worldTileY) {

		Tile fTile = null;

		for (Component component : getComponents()) {
			Tile foregroundTile = component.getForegroundTile(worldTileX, worldTileY);

			if (foregroundTile != null) {
				if (foregroundTile.isPlatformTile) {
					fTile = foregroundTile;
					break;
				} else if (foregroundTile instanceof EmptyTile) {
					fTile = new Tile.EmptyTile();
				} else if (!(fTile instanceof EmptyTile)) {
					if (fTile instanceof EmptyTile) {
						continue;
					}
					fTile = foregroundTile;
				}
			}
		}

		if (fTile != null) {
			return fTile;
		}

		return internalGetForegroundTile(worldTileX, worldTileY);
	}


	/**
	 * Get the background tile from the structure.
	 *
	 * @param worldTileX - world tile coordinates
	 * @param worldTileY - world tile coordinates
	 * @return - the {@link Tile} the {@link Structure} says it is.
	 */
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		for (Component component : getComponents()) {
			Tile backgroundTile = component.getBackgroundTile(worldTileX, worldTileY);
			if (backgroundTile != null) {
				return backgroundTile;
			}
		}

		return internalGetBackgroundTile(worldTileX, worldTileY);
	}


	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + this.hashCode();
	}


	/**
	 * See {@link #structureKey}
	 */
	protected int getStructureKey() {
		return structureKey;
	}


	/**
	 * Sets the structure key of this structure
	 */
	protected void setStructureKey(int structureKey) {
		this.structureKey = structureKey;
	}


	/**
	 * See {@link #chunksLeftToBeGenerated}
	 */
	public int getChunksLeftToBeGenerated() {
		return chunksLeftToBeGenerated;
	}


	/**
	 * See {@link #chunksLeftToBeGenerated}
	 */
	public void setChunksLeftToBeGenerated(int chunksLeftToBeGenerated) {
		this.chunksLeftToBeGenerated = chunksLeftToBeGenerated;
	}


	/**
	 * @return the {@link #components} - list of all {@link Component}s on this {@link Structure}
	 */
	public List<Component> getComponents() {
		return components;
	}
}