package spritestar.world.generation;

import java.util.ArrayList;
import java.util.HashMap;

import spritestar.persistence.ParameterPersistenceService;
import spritestar.util.datastructure.ConcurrentIntIntToIntHashMap;
import spritestar.util.datastructure.ConcurrentIntObjectHashMap;
import spritestar.util.datastructure.IntIntHashMap;
import spritestar.util.datastructure.TwoInts;
import spritestar.world.topography.tile.Tile;

/**
 * Stores mappings of structures to keys and keys to chunks.
 *
 * Contains all the methods for interacting with this storage system.
 *
 * @author Sam, Matt
 */
public class StructureMap {

	/** Stores a key on some chunk coordinates. */
	public static ConcurrentIntIntToIntHashMap superStructureKeys;

	/** Stores a key on some chunk coordinates. */
	public static ConcurrentIntIntToIntHashMap subStructureKeys;

	/** Stores which structure corresponds to which key. */
	public static ConcurrentIntObjectHashMap<Structure> structures;

	// Used to keep the surface height consistent between structures where needed.
	public static HashMap<Integer, Integer> surfaceHeight;

	/**
	 * Adds a {@link SuperStructure} to the {@link #structures} map and
	 * populates all relevant values on {@link #superStructureKeys}.
	 *
	 * Chunk Coordinates:
	 *
	 * @param leftEdge
	 * @param topEdge
	 * @param rightEdge
	 * @param bottomEdge
	 *
	 * @param structure - {@link SuperStructure} to add.
	 */
	public static int addSuperStructure(int leftEdge, int topEdge, int rightEdge, int bottomEdge, SuperStructure structure) {
		return addStructure(leftEdge, topEdge, rightEdge, bottomEdge, structure, true);
	}


	/**
	 * Adds a {@link SuperStructure} to the {@link #structures} map and
	 * populates all relevant values on {@link #superStructureKeys}.
	 *
	 * @param chunksToAdd - the list of coordinates this structure spans.
	 *
	 * @param structure - {@link SuperStructure} to add.
	 */
	public static int addSuperStructure(ArrayList<TwoInts> chunksToAdd, SuperStructure structure) {
		return addStructure(chunksToAdd, structure, true);
	}

	/**
	 * Adds a {@link SubStructure} to the {@link #structures} map and populates
	 * all relevant values on {@link #subStructureKeys}.
	 *
	 * Chunk Coordinates:
	 *
	 * @param leftEdge
	 * @param topEdge
	 * @param rightEdge
	 * @param bottomEdge
	 *
	 * @param structure - {@link SubStructure} to add.
	 */
	public static int addSubStructure(int leftEdge, int topEdge, int rightEdge, int bottomEdge, SubStructure structure) {
		return addStructure(leftEdge, topEdge, rightEdge, bottomEdge, structure, false);
	}


	/**
	 * Adds a {@link SubStructure} to the {@link #structures} map and
	 * populates all relevant values on {@link #subStructureKeys}.
	 *
	 * @param chunksToAdd - the list of coordinates this structure spans.
	 *
	 * @param structure - {@link SubStructure} to add.
	 */
	public static int addSubStructure(ArrayList<TwoInts> chunksToAdd, SubStructure structure) {
		return addStructure(chunksToAdd, structure, false);
	}


	/**
	 * Gets a {@link SuperStructure} at the specified coordinates.
	 *
	 * @param chunkX - the chunk x coordinate
	 * @param chunkY - the chunk y coordinate
	 *
	 * @return - the {@link SuperStructure}
	 */
	public static Structure getSuperStructure(int chunkX, int chunkY) {
		return getStructure(chunkX, chunkY, true);
	}


	/**
	 * Gets a {@link SubStructure} at the specified coordinates.
	 *
	 * @param chunkX - the chunk coordinate
	 * @param chunkY - the chunk coordinate
	 *
	 * @return - the {@link SubStructure}
	 */
	public static Structure getSubStructure(int chunkX, int chunkY) {
		return getStructure(chunkX, chunkY, false);
	}


	/**
	 * Looks for a substructure and tries to get a tile, if it's null, it gets
	 * one from super structure.
	 *
	 * @param chunkX - the chunk coord you're looking at.
	 * @param chunkY - the chunk coord you're looking at.
	 * @param worldTileX - the world tile coord you're looking at.
	 * @param worldTileY - the world tile coord you're looking at.
	 * @param foreground - true if want the foreground tile, false, the background.
	 * @return - the {@link Tile}
	 */
	public static Tile getTile(int chunkX, int chunkY, int worldTileX, int worldTileY, boolean foreground) {
		Tile returnTile = null;
		if (getSubStructure(chunkX, chunkY) != null) {
			returnTile = foreground ?
				getSubStructure(chunkX, chunkY).getForegroundTile(worldTileX, worldTileY) :
				getSubStructure(chunkX, chunkY).getBackgroundTile(worldTileX, worldTileY);
		}
		if (returnTile == null) {
			if (getSuperStructure(chunkX, chunkY) != null) {
				returnTile = foreground ?
					getSuperStructure(chunkX, chunkY).getForegroundTile(worldTileX, worldTileY) :
					getSuperStructure(chunkX, chunkY).getBackgroundTile(worldTileX, worldTileY);
			}
		}
		return returnTile;
	}


	/**
	 * Determines if the {@link SuperStructure} at the specified coordinates
	 * exists.
	 *
	 * @param chunkX - the chunk x coordinate
	 * @param chunkY - the chunk y coordinate
	 * @return whether the structure exists
	 */
	public static boolean doesSuperStructureExist(int chunkX, int chunkY) {
		return doesStructureExist(chunkX, chunkY, true);
	}


	/**
	 * Determines if the {@link SubStructure} at the specified coordinates
	 * exists.
	 *
	 * @param chunkX - the chunk coordinate
	 * @param chunkY - the chunk coordinate
	 * @return whether the structure exists or not
	 */
	public static boolean doesSubStructureExist(int chunkX, int chunkY) {
		return doesStructureExist(chunkX, chunkY, false);
	}


	/**
	 * Deletes a {@link SuperStructure} from memory
	 */
	public static void deleteSuperStructure(int chunkX, int chunkY) {
		deleteStructure(chunkX, chunkY, true);
	}


	/**
	 * Deletes a {@link SubStructure} from memory
	 */
	public static void deleteSubStructure(int chunkX, int chunkY) {
		deleteStructure(chunkX, chunkY, false);
	}


	/**
	 * Gets a {@link SuperStructure} or {@link SubStructure} from the key maps.
	 */
	private static Structure getStructure(int chunkX, int chunkY, boolean superStructure) {
		ConcurrentIntIntToIntHashMap structureKeysToUse = superStructure ? superStructureKeys : subStructureKeys;

		if (structureKeysToUse.get(chunkX) == null) {
			return null;
		} else if (structureKeysToUse.get(chunkX).get(chunkY) == null) {
			return null;
		} else {
			if (structures.get((int) structureKeysToUse.get(chunkX).get(chunkY)) == null) {
				return null;
			} else {
				return structures.get((int) structureKeysToUse.get(chunkX).get(chunkY));
			}
		}
	}


	/**
	 * Gets a {@link SuperStructure} or {@link SubStructure} from the key maps.
	 */
	public static Structure getStructure(int key) {
		return structures.get(key);
	}


	/**
	 * Determines whether a {@link Structure} in the key maps.
	 */
	public static boolean doesStructureExist(int chunkX, int chunkY, boolean superStructure) {
		ConcurrentIntIntToIntHashMap structureKeysToUse = superStructure ? superStructureKeys : subStructureKeys;

		if (structureKeysToUse.get(chunkX) == null) {
			return false;
		} else {
			return structureKeysToUse.get(chunkX).get(chunkY) != null;
		}
	}


	/**
	 * Deletes a {@link Structure} from one of the key maps.
	 */
	private static void deleteStructure(int chunkX, int chunkY, boolean superStructure) {
		ConcurrentIntIntToIntHashMap structureKeysToUse = superStructure ? superStructureKeys : subStructureKeys;

		int key = structureKeysToUse.get(chunkX).get(chunkY);
		structureKeysToUse.get(chunkX).remove(chunkY);
		structures.get(key).chunksLeftToBeGenerated--;
		if (structures.get(key).isFinishedGenerating()) {
			structures.remove(key);
		}
	}


	/**
	 * Adds a {@link Structure} to the {@link #structures} map and populates all
	 * relevent values on {@link #subStructureKeys} or
	 * {@link #superStructureKeys} depending on desired operation.
	 *
	 * Chunk Coordinates:
	 *
	 * @param leftEdge
	 * @param topEdge
	 * @param rightEdge
	 * @param bottomEdge
	 *
	 * @param structure - {@link Structure} to add.
	 * @param superStructure - true if you're adding a {@link SuperStructure}
	 */
	private static int addStructure(int leftEdge, int topEdge, int rightEdge, int bottomEdge, Structure structure, boolean superStructure) {
		ConcurrentIntIntToIntHashMap structureKeys = superStructure ? superStructureKeys : subStructureKeys;

		int structureKey = ParameterPersistenceService.getParameters().getStructureKey();
		if (structures.get(structureKey) == null) {
			structures.put(structureKey, structure);
		} else {
			throw new RuntimeException("key already used");
		}
		for (int x = leftEdge; x <= rightEdge; x++) {
			if (structureKeys.get(x) == null) {
				structureKeys.put(x, new IntIntHashMap());
			}
			for (int y = topEdge; y >= bottomEdge; y--) {
				if (structureKeys.get(x).get(y) != null) {
					throw new RuntimeException("Overwriting structures is not allowed.");
				}
				structureKeys.get(x).put(y, structureKey);
			}
		}
		ParameterPersistenceService.getParameters().incrementStructureKey();
		return structureKey;
	}


	/**
	 * Adds a {@link Structure} to the {@link #structures} map and populates all
	 * relevant values on {@link #subStructureKeys} or
	 * {@link #superStructureKeys} depending on desired operation.
	 *
	 * @param chunksToAdd - the List of coordinates this structure spans.
	 *
	 * @param structure - {@link Structure} to add.
	 * @param superStructure - true if you're adding a {@link SuperStructure}
	 */
	private static int addStructure(ArrayList<TwoInts> chunksToAdd, Structure structure, boolean superStructure) {
		ConcurrentIntIntToIntHashMap structureKeys = superStructure ? superStructureKeys : subStructureKeys;

		int structureKey = ParameterPersistenceService.getParameters().getStructureKey();
		if (structures.get(structureKey) == null) {
			structures.put(structureKey, structure);
		} else {
			throw new RuntimeException("key already used");
		}
		for(TwoInts coordinate : chunksToAdd) {
			if (structureKeys.get(coordinate.a) == null) {
				structureKeys.put(coordinate.a, new IntIntHashMap());
			}
			if (structureKeys.get(coordinate.a).get(coordinate.b) != null) {
				throw new RuntimeException("Overwriting structures is not allowed.");
			}
			structureKeys.get(coordinate.a).put(coordinate.b, structureKey);
		}
		ParameterPersistenceService.getParameters().incrementStructureKey();
		return structureKey;
	}
}
