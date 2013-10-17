package spritestar.world.generation;

import java.io.Serializable;
import java.util.ArrayList;

import spritestar.world.topography.Chunk;
import spritestar.world.topography.tile.Tile;

/**
 * A {@link Structure} which can later be used to generate a {@link Chunk}
 *
 * @author Sam, Matt
 */
public abstract class Structure implements Serializable {
	private static final long serialVersionUID = -5890196858721145717L;

	/** The key used by this {@link Structure} in the {@link StructureMap} */
	public int structureKey;

	/** The number of chunks this structure has left to generate on it */
	public int chunksLeftToBeGenerated = -1;

	/** {@link Component}s that are part of this {@link SubStructure} */
	protected ArrayList<Component> components = new ArrayList<Component>();

	/** A counter to keep track of which layer {@link Structure#addComponent} adds a component on. */
	private int layerCounter = 0;


	/**
	 * @return whether this {@link Structure} has finished generating.
	 */
	public boolean isFinishedGenerating() {
		return chunksLeftToBeGenerated == 0;
	}


	/** Calculate how many chunks the Structure includes. The structure will be deleted when this many have been generated.*/
	protected abstract void calculateChunksToGenerate();


	/**
	 * Checks is the component is ok to be added to the {@link components} .
 	 * Performs the tasks to try and make it ok if neccessary.
	 *
	 * @param component the component you want to check
	 * @return - true if the component is ok to be added.
	 */
	protected abstract boolean checkComponent(Component component);


	/**
	 * Adds a {@link Component} to {@link #components}
	 */
	protected void addComponent(Component component) {
		if(!checkComponent(component)) {
			return;
		}
		components.add(layerCounter, component);
		layerCounter++;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + this.hashCode();
	}


	/**
	 * Makes the structure.
	 * Adds the structure to {@link StructureMap}.
	 * Calculates and sets extra parameters.
	 *
	 * @param startingChunkX - the chunk coordinates to start generating the structure from
	 * @param startingChunkY - the chunk coordinates to start generating the structure from
	 * @param generatingToRight - true if generating to the right
	 */
	public void generateAndFinalize(int startingChunkX, int startingChunkY, boolean generatingToRight) {
		generate(startingChunkX, startingChunkY, generatingToRight);
		setKeys(addToStructureMap());
		calculateChunksToGenerate();
	}


	/**
	 * Makes the structure.
	 *
	 * @param startingChunkX - the chunk coordinates to start generating the structure from
	 * @param startingChunkY - the chunk coordinates to start generating the structure from
	 * @param generatingToRight - true if generating to the right
	 */
	protected abstract void generate(int startingChunkX, int startingChunkY, boolean generatingToRight);


	/**
	 * Adds a structure to the {@link StructureMap}.
	 *
	 * @return - the key used by this structure on the {@link StructureMap}.
	 */
	protected abstract int addToStructureMap();


	/**
	 * Sets the key on this structure and all components stored within it.
	 *
	 * @param key - The key used by this {@link Structure} in the {@link StructureMap}
	 */
	private void setKeys(int key) {
		this.structureKey = key;
		for(Component component : components) {
			component.setKeys(key);
		}
	}


	/**
	 * Generate the {@link Structure}.
	 */
	protected abstract void generateStructure(boolean generatingToRight);


	/**
	 * Get a foreground tile from the {@link Structure}.
	 *
	 * @param worldTileX - world tile coordinates
	 * @param worldTileY - world tile coordinates
	 * @return - the {@link Tile} the {@link Structure} says it is.
	 */
	public abstract Tile getForegroundTile(int worldTileX, int worldTileY);


	/**
	 * Get the background tile from the structure.
	 *
	 * @param worldTileX - world tile coordinates
	 * @param worldTileY - world tile coordinates
	 * @return - the {@link Tile} the {@link Structure} says it is.
	 */
	public abstract Tile getBackgroundTile(int worldTileX, int worldTileY);
}