package bloodandmithril.generation;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Contains maps from coordinates to (super/sub) {@link Structure} keys.
 * Contains maps from (super/sub) structure keys to {@link Structure}s
 * Contains all the methods for interacting with this storage system.
 *
 * @author Matt
 */
public class Structures {

	/** Stores a key on some chunk coordinates. */
	private static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> superStructureKeys;

	/** Stores a key on some chunk coordinates. */
	private static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> subStructureKeys;

	/** Stores which structure corresponds to which key. */
	private static ConcurrentHashMap<Integer, Structure> structures;

	// Used to keep the surface height consistent between structures where needed.
	private static HashMap<Integer, Integer> surfaceHeight;

	/**
	 * Looks for a substructure and attempts to get a tile, if it's null, get
	 * one from the super structure.
	 * 
	 * This method enforces that substructures sit on top of superstructures, thus overriding them.
	 *
	 * @param chunkX
	 * @param chunkY
	 * @param worldTileX
	 * @param worldTileY
	 * @param foreground
	 */
	public static Tile getTile(int chunkX, int chunkY, int worldTileX, int worldTileY, boolean foreground) {
		
		Tile returnTile = null;
		
		if (getStructure(chunkX, chunkY, false) != null) {
			returnTile = 
				foreground ?
				getStructure(chunkX, chunkY, false).getForegroundTile(worldTileX, worldTileY):
				getStructure(chunkX, chunkY, false).getBackgroundTile(worldTileX, worldTileY);
		}
		
		if (returnTile == null) {
			if (getStructure(chunkX, chunkY, true) != null) {
				returnTile = 
					foreground ?
					getStructure(chunkX, chunkY, true).getForegroundTile(worldTileX, worldTileY):
					getStructure(chunkX, chunkY, true).getBackgroundTile(worldTileX, worldTileY);
			}
		}
		
		return returnTile;
	}


	/**
	 * Gets the underlying {@link SuperStructure} or {@link SubStructure} from the key maps, given chunk coordinates.
	 */
	private static Structure getStructure(int chunkX, int chunkY, boolean superStructure) {
		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> keyMapToUse = superStructure ? getSuperStructureKeys() : getSubStructureKeys();

		if (keyMapToUse.get(chunkX) == null) {
			return null;
		} else if (keyMapToUse.get(chunkX).get(chunkY) == null) {
			return null;
		} else {
			if (getStructures().get((int) keyMapToUse.get(chunkX).get(chunkY)) == null) {
				return null;
			} else {
				return getStructures().get((int) keyMapToUse.get(chunkX).get(chunkY));
			}
		}
	}
	
	
	/**
	 * Returns a structure, given a key.
	 */
	public static Structure get(int key) {
		return getStructures().get(key);
	}


	/**
	 * Determines whether a {@link Structure} in the key maps.
	 */
	public static boolean structureExists(int chunkX, int chunkY, boolean superStructure) {
		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> keyMapToUse = superStructure ? getSuperStructureKeys() : getSubStructureKeys();

		if (keyMapToUse.get(chunkX) == null) {
			return false;
		} else {
			return keyMapToUse.get(chunkX).get(chunkY) != null;
		}
	}


	/**
	 * Deletes a {@link Chunk} from the relevant keyMap, decrements number of chunks left to be generated, also deletes the structure if it was the last chunk.
	 */
	public static void deleteChunkFromStructureKeyMapAndCheckIfStructureCanBeDeleted(int chunkX, int chunkY, boolean superStructure) {
		
		// Grab the specified key map
		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> keyMapToUse = superStructure ? getSuperStructureKeys() : getSubStructureKeys();

		// Delete the key from key map
		int key = keyMapToUse.get(chunkX).get(chunkY);
		keyMapToUse.get(chunkX).remove(chunkY);
		
		// Decrement chunks left to be generated
		getStructures().get(key).setChunksLeftToBeGenerated(
				getStructures().get(key).getChunksLeftToBeGenerated() - 1);
		
		// If that was the last chunk, delete the structure, no longer needed
		if (getStructures().get(key).allChunksGenerated()) {
			getStructures().remove(key);
		}
	}


	/**
	 * Adds a {@link Structure} to the {@link #structures} map and populates all
	 * relevant values on {@link #subStructureKeys} or {@link #superStructureKeys} depending on desired operation.
	 *
	 * @param boundaries - Chunk coordinate boundaries.
	 * @param structure - {@link Structure} to add.
	 * @param superStructure - true if you're adding a {@link SuperStructure}
	 */
	public static int addStructure(Boundaries boundaries, Structure structure, boolean superStructure) {
		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> structureKeys = superStructure ? getSuperStructureKeys() : getSubStructureKeys();

		int structureKey = ParameterPersistenceService.getParameters().getNextStructureKey();
		if (getStructures().get(structureKey) == null) {
			getStructures().put(structureKey, structure);
		} else {
			throw new RuntimeException("key already used");
		}
		for (int x = boundaries.left; x <= boundaries.right; x++) {
			if (structureKeys.get(x) == null) {
				structureKeys.put(x, new ConcurrentHashMap<Integer, Integer>());
			}
			for (int y = boundaries.top; y >= boundaries.bottom; y--) {
				if (structureKeys.get(x).get(y) != null) {
					throw new RuntimeException("Overwriting structures is not allowed.");
				}
				structureKeys.get(x).put(y, structureKey);
			}
		}
		return structureKey;
	}


	/**
	 * See {@link #structures}
	 */
	public static ConcurrentHashMap<Integer, Structure> getStructures() {
		return structures;
	}


	/**
	 * See {@link #structures}
	 */
	public static void setStructures(ConcurrentHashMap<Integer, Structure> structures) {
		Structures.structures = structures;
	}


	/**
	 * See {@link #superStructureKeys}
	 */
	public static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> getSuperStructureKeys() {
		return superStructureKeys;
	}


	/**
	 * See {@link #superStructureKeys}
	 */
	public static void setSuperStructureKeys(ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> superStructureKeys) {
		Structures.superStructureKeys = superStructureKeys;
	}


	/**
	 * See {@link #subStructureKeys}
	 */
	public static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> getSubStructureKeys() {
		return subStructureKeys;
	}


	/**
	 * See {@link #subStructureKeys}
	 */
	public static void setSubStructureKeys(ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> subStructureKeys) {
		Structures.subStructureKeys = subStructureKeys;
	}


	/**
	 * See {@link #surfaceHeight}
	 */
	public static HashMap<Integer, Integer> getSurfaceHeight() {
		return surfaceHeight;
	}


	/**
	 * See {@link #surfaceHeight}
	 */
	public static void setSurfaceHeight(HashMap<Integer, Integer> surfaceHeight) {
		Structures.surfaceHeight = surfaceHeight;
	}
}
