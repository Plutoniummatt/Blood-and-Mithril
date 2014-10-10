package bloodandmithril.performance;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;

import com.google.common.collect.Sets;

/**
 * A positional index node is an entity which holds references to world objects depending on their world positions.  This will be used as a 'search index' so to speak
 * to reduce performance overhead by avoiding the need to iterate over all objects that exist
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class PositionalIndexNode implements Serializable {
	private static final long serialVersionUID = -8256634999855942751L;

	private Set<Integer> props = Sets.newConcurrentHashSet();
	private Set<Integer> individuals = Sets.newConcurrentHashSet();

	/**
	 * Constructor
	 */
	public PositionalIndexNode() {}

	/**
	 * @return all entities that are indexed by this node.
	 */
	public synchronized Collection<Integer> getAllEntitiesForType(Class<?> clazz) {
		if (clazz.equals(Individual.class)) {
			return individuals;
		}

		if (clazz.equals(Prop.class)) {
			return props;
		}

		throw new RuntimeException("Unrecognised class: " + clazz.getSimpleName());
	}
}