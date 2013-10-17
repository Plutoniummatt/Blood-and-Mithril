package spritestar.world.generation;

import spritestar.util.datastructure.Boundaries;
import spritestar.world.topography.ChunkMap;

/**
 * A {@link SuperStructure} is a {@link Structure} that is generated on top of null positions in the {@link ChunkMap} and {@link StructureMap}.
 *
 * @author Sam, Matt
 */
public abstract class SuperStructure extends Structure {
	private static final long serialVersionUID = -4187785116665052403L;
	
	/** The edges of this SuperStructure */
	public Boundaries boundaries;
	
	/**
	 * Finds Space for the structure.
	 * Checks it's validity
	 * Makes the structure.
	 * Adds the structure you just made to the {@link StructureMap}.
	 * 
	 * @param startingChunkX - the chunk coordinates to start generating the structure from
	 * @param startingChunkY - the chunk coordinates to start generating the structure from
	 * @param generatingToRight - true if generating to the right
	 */
	@Override
	public void generate(int startingChunkX, int startingChunkY, boolean generatingToRight) {
		boundaries = findSpace(startingChunkX, startingChunkY);
		if (!isValid()) {
			return;
		}
		
		// Generate this SuperStructure
		generateStructure(generatingToRight);
		
		// Generate SubStructures belonging to this SuperStructure
		generateSubStructures(generatingToRight);
	}
	
	
	/**
	 * Find space for the structure
	 * @return - the four boundaries of the structure, top, bottom, left, right
	 */
	protected abstract Boundaries findSpace(int startingChunkX, int startingChunkY);
	

	/**
	 * Used to test whether the structure is valid, for example, if the space calculated is too small to fit the structure in.
	 * If the structure isn't valid, generation will stop.
	 */
	protected abstract boolean isValid();
	
	@Override
	protected boolean checkComponent(Component component) {
		return (component.boundaries.top <= this.boundaries.top && 
				component.boundaries.bottom >= this.boundaries.bottom && 
				component.boundaries.left >= this.boundaries.left && 
				component.boundaries.right <= this.boundaries.right
				);
	}
	
	@Override
	protected int addToStructureMap() {
		return StructureMap.addSuperStructure(boundaries.left, boundaries.top, boundaries.right, boundaries.bottom, this);
	}
	
	
	/**
	 * Adds a {@link SubStructure} to this {@link SuperStructure}
	 */
	protected void generateAndAddSubStructure(SubStructure subStructure, int startingChunkX, int startingChunkY, boolean generatingToRight) {
		subStructure.superStructureBoundaries = this.boundaries;
		subStructure.generateAndFinalize(startingChunkX, startingChunkY, generatingToRight);
	}
	
	
	/**
	 * Calculates how many chunks are in the structure. 
	 * This is used to know when to delete the structure from the StructureMap.
	 */
	@Override
	protected void calculateChunksToGenerate() {
		if (chunksLeftToBeGenerated == -1) {
			chunksLeftToBeGenerated = (boundaries.top - boundaries.bottom + 1) * (boundaries.right - boundaries.left + 1);
		} else {
			throw new RuntimeException("chunksLeftToBeGenerated has already been calculated");
		}
	}
	
	
	/** Generates any {@link SubStructure}s on this {@link SuperStructure} */
	protected abstract void generateSubStructures(boolean generatingToRight);
}