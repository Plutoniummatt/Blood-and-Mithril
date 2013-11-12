package bloodandmithril.generation;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Stores mappings of structures to keys and keys to chunks.
 *
 * Contains all the methods for interacting with this storage system.
 *
 * @author Sam, Matt
 */
public class StructureMap {

	/** Stores a key on some chunk coordinates. */
	public static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> superStructureKeys;

	/** Stores a key on some chunk coordinates. */
	public static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> subStructureKeys;

	/** Stores which structure corresponds to which key. */
	public static ConcurrentHashMap<Integer, Structure> structures;

	// Used to keep the surface height consistent between structures where needed.
	public static HashMap<Integer, Integer> surfaceHeight;


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
		
		if (getStructure(chunkX, chunkY, false) != null) {
			returnTile = foreground ?
				getStructure(chunkX, chunkY, false).getForegroundTile(worldTileX, worldTileY) :
				getStructure(chunkX, chunkY, false).getBackgroundTile(worldTileX, worldTileY);
		}
		
		if (returnTile == null) {
			if (getStructure(chunkX, chunkY, true) != null) {
				returnTile = foreground ?
					getStructure(chunkX, chunkY, true).getForegroundTile(worldTileX, worldTileY) :
					getStructure(chunkX, chunkY, true).getBackgroundTile(worldTileX, worldTileY);
			}
		}
		
		return returnTile;
	}


	/**
	 * Gets a {@link SuperStructure} or {@link SubStructure} from the key maps.
	 */
	private static Structure getStructure(int chunkX, int chunkY, boolean superStructure) {
		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> keyMapToUse = superStructure ? superStructureKeys : subStructureKeys;

		if (keyMapToUse.get(chunkX) == null) {
			return null;
		} else if (keyMapToUse.get(chunkX).get(chunkY) == null) {
			return null;
		} else {
			if (structures.get((int) keyMapToUse.get(chunkX).get(chunkY)) == null) {
				return null;
			} else {
				return structures.get((int) keyMapToUse.get(chunkX).get(chunkY));
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
		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> keyMapToUse = superStructure ? superStructureKeys : subStructureKeys;

		if (keyMapToUse.get(chunkX) == null) {
			return false;
		} else {
			return keyMapToUse.get(chunkX).get(chunkY) != null;
		}
	}


	/**
	 * Deletes a {@link Structure} from one of the key maps if it has finished generating.
	 * 
	 * Otherwise, removes the key from the specified key map and decrements {@link Structure#chunksLeftToBeGenerated}
	 */
	public static void structureDeletionCheck(int chunkX, int chunkY, boolean superStructure) {
		
		// Grab the specified key map
		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> keyMapToUse = superStructure ? superStructureKeys : subStructureKeys;

		// Delete the key from key map
		int key = keyMapToUse.get(chunkX).get(chunkY);
		keyMapToUse.get(chunkX).remove(chunkY);
		
		// Decrement chunks left to be generated
		structures.get(key).chunksLeftToBeGenerated--;
		
		// If that was the last chunk, delete the structure, no longer needed
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
	public static int addStructure(int leftEdge, int topEdge, int rightEdge, int bottomEdge, Structure structure, boolean superStructure) {
		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> structureKeys = superStructure ? superStructureKeys : subStructureKeys;

		int structureKey = ParameterPersistenceService.getParameters().getNextStructureKey();
		if (structures.get(structureKey) == null) {
			structures.put(structureKey, structure);
		} else {
			throw new RuntimeException("key already used");
		}
		for (int x = leftEdge; x <= rightEdge; x++) {
			if (structureKeys.get(x) == null) {
				structureKeys.put(x, new ConcurrentHashMap<Integer, Integer>());
			}
			for (int y = topEdge; y >= bottomEdge; y--) {
				if (structureKeys.get(x).get(y) != null) {
					throw new RuntimeException("Overwriting structures is not allowed.");
				}
				structureKeys.get(x).put(y, structureKey);
			}
		}
		return structureKey;
	}
}
