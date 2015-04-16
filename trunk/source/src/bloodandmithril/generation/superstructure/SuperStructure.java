package bloodandmithril.generation.superstructure;

import java.util.Set;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.ChunkMap;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

/**
 * A {@link SuperStructure} is a {@link Structure} that is generated on top of null positions in the {@link ChunkMap} and {@link Structures}.
 *
 * @author Sam, Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class SuperStructure extends Structure {
	private static final long serialVersionUID = -4187785116665052403L;

	/** The edges of this SuperStructure, in chunk coordinates */
	private Boundaries boundaries;

	protected final Set<TwoInts> startingLocations = Sets.newHashSet();

	// Used to keep the surface height consistent between structures where needed.
	private SerializableMappingFunction<Integer, Integer> surfaceHeight;

	/**
	 * Constructor
	 */
	protected SuperStructure(int worldId) {
		super(worldId);
	}


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


	public Set<Vector2> getPossibleStartingLocations() {
		return Sets.newHashSet(Collections2.transform(startingLocations, location -> {
			return new Vector2(
				Topography.convertToWorldCoord(location.a, false),
				Topography.convertToWorldCoord(location.b, false)
			);
		}));
	}


	/**
	 * Find space for the structure
	 * @return - the four boundaries of the structure, top, bottom, left, right
	 */
	protected abstract Boundaries findSpace(int startingChunkX, int startingChunkY);


	@Override
	protected int addToStructureMap() {
		return Domain.getWorld(worldId).getTopography().getStructures().addStructure(getBoundaries(), this, true);
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
	public Boundaries getBoundaries() {
		return boundaries;
	}


	/**
	 * See {@link #boundaries}
	 */
	private void setBoundaries(Boundaries boundaries) {
		this.boundaries = boundaries;
	}


	/**
	 * See {@link #surfaceHeight}
	 */
	public Function<Integer, Integer> getSurfaceHeight() {
		return surfaceHeight;
	}


	/**
	 * See {@link #surfaceHeight}
	 */
	public void setSurfaceHeight(SerializableMappingFunction<Integer, Integer> surfaceHeight) {
		this.surfaceHeight = surfaceHeight;
	}
}