package spritestar.generation;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Contains data structures responsible for the storage and maintenance of {@link Structure}s
 *
 * @author Matt
 */
public class Structures {

	/** Data structure for storing {@link Structure}s that have not yet been fully generated */
	public static ConcurrentHashMap<Integer, Structure> structures = new ConcurrentHashMap<>();

	/** Maps a chunk coordinate to the unique key of a {@link Structure} that can be used to retrieve the {@link Structure} from {@link #structures} */
	public static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> superStructureKeyMap = new ConcurrentHashMap<>();

	/** Maps a chunk coordinate to the unique key of a {@link Structure} that can be used to retrieve the {@link Structure} from {@link #structures} */
	public static ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> subStructureKeyMap = new ConcurrentHashMap<>();

	/** Gets a structure from the specified map, returning null if non exists */
	public static Structure getStructure(int chunkX, int chunkY, boolean superStructure) {

		// Determine which map we're interested in
		ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> map = superStructure ? superStructureKeyMap : subStructureKeyMap;

		// Perform null checks
		if (map.get(chunkX) == null || map.get(chunkX).get(chunkY) == null) {
			return null;
		}

		// Get the structure from structures if key exists
		return structures.get(map.get(chunkX).get(chunkY));
	}
}
