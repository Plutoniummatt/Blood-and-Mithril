package bloodandmithril.generation.superstructure;

import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.ChunkMap;

/**
 * A {@link SuperStructure} is a {@link Structure} that is generated on top of null positions in the {@link ChunkMap} and {@link Structures}.
 *
 * @author Sam, Matt
 */
public abstract class SuperStructure extends Structure {
	private static final long serialVersionUID = -4187785116665052403L;

	/** The edges of this SuperStructure */
	private Boundaries boundaries;

	/**
	 * Finds Space for the structure.
	 * Generates the structure.
	 *
	 * @param startingChunkX - the chunk coordinates to start generating the structure from
	 * @param startingChunkY - the chunk coordinates to start generating the structure from
	 * @param generatingToRight - true if generating to the right
	 */
	@Override
	protected void findSpaceAndAddToMap(int startingChunkX, int startingChunkY, boolean generatingToRight) {

		// Find space for this super structure
		setBoundaries(findSpace(startingChunkX, startingChunkY));

		// Add to map
		setStructureKey(addToStructureMap());
	}


	/**
	 * Find space for the structure
	 * @return - the four boundaries of the structure, top, bottom, left, right
	 */
	protected abstract Boundaries findSpace(int startingChunkX, int startingChunkY);


	@Override
	protected int addToStructureMap() {
		return Structures.addStructure(getBoundaries(), this, true);
	}


	/**
	 * Calculates how many chunks are in the structure.
	 * This is used to know when to delete the structure from the StructureMap.
	 */
	@Override
	protected void calculateChunksToGenerate() {
		if (getChunksLeftToBeGenerated() == -1) {
			setChunksLeftToBeGenerated((getBoundaries().top - getBoundaries().bottom + 1) * (getBoundaries().right - getBoundaries().left + 1));
		} else {
			throw new RuntimeException("chunksLeftToBeGenerated has already been calculated");
		}
	}


	/**
	 * See {@link #boundaries}
	 */
	protected Boundaries getBoundaries() {
		return boundaries;
	}


	/**
	 * See {@link #boundaries}
	 */
	private void setBoundaries(Boundaries boundaries) {
		this.boundaries = boundaries;
	}
}