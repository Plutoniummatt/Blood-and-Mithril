package spritestar.generation;

import spritestar.persistence.ParameterPersistenceService;
import spritestar.world.topography.tile.Tile;

/**
 * A Structure is an abstract topological entity.  It is stored in {@link Structure#structures} with a unique key assigned to it upon creation
 *
 * @author Matt
 */
public abstract class Structure {

	/** Number of chunks left to be generated before this {@link Structure} can be deleted */
	private int chunksLeftToGenerate;

	/** The unique key of this {@link Structure} */
	private final int key;

	/**
	 * Constructor
	 */
	public Structure() {
		this.key = ParameterPersistenceService.getParameters().getNextStructureKey();
		Structures.structures.put(key, this);
	}


	/** True if {@link #chunksLeftToGenerate} is zero, ie we have no more chunks left pending generation */
	public boolean isFinishedGenerating() {
		return chunksLeftToGenerate == 0;
	}


	/** Removes this {@link Structure} from {@link #structures} */
	public void delete() {
		Structures.structures.remove(key);
	}


	/** Adds the {@link #key} of this structure to the specified structure key map */
	public abstract void addToStructureMap(boolean superStructure);

	/** Calculates how many chunks this {@link Structure} includes */
	public abstract void calculateChunksToGenerate();

	/** Returns the foreground {@link Tile}, if coordinates are outside of the structure, returns null */
	public abstract Class<? extends Tile> getForegroundTile(int tileX, int tileY);

	/** Returns the background {@link Tile}, if coordinates are outside of the structure, returns null */
	public abstract Class<? extends Tile> getBackgroundTile(int tileX, int tileY);
}