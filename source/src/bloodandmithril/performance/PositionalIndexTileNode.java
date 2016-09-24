package bloodandmithril.performance;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.fluids.FluidParticle;

/**
 * A positional index node is an entity which holds references to world objects depending on their world positions.  This will be used as a 'search index' so to speak
 * to reduce performance overhead by avoiding the need to iterate over all objects that exist
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2014")
public class PositionalIndexTileNode implements Serializable {
	private static final long serialVersionUID = -8256634999855942751L;

	private Set<Long> fluidParticles = Sets.newConcurrentHashSet();

	/**
	 * Constructor
	 */
	public PositionalIndexTileNode() {}

	/**
	 * @return all entities that are indexed by this node.
	 */
	public synchronized Collection<Long> getAllEntitiesForType(Class<?> clazz) {
		if (clazz.equals(FluidParticle.class)) {
			return fluidParticles;
		}

		throw new RuntimeException("Unrecognised class: " + clazz.getSimpleName());
	}


	public void addFluidParticle(long key) {
		fluidParticles.add(key);
	}
	
	
	public void removeFluidParticle(long key) {
		fluidParticles.remove(key);
	}


	public void clear() {
		fluidParticles.clear();
	}
}