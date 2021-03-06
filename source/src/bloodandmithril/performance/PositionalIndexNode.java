package bloodandmithril.performance;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.fluids.FluidColumn;

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
	private Set<Integer> items = Sets.newConcurrentHashSet();
	private Set<Integer> fluidColumns = Sets.newConcurrentHashSet();

	/**
	 * Constructor
	 */
	public PositionalIndexNode() {}

	/**
	 * @return all entities that are indexed by this node.
	 */
	public synchronized Collection<Integer> getAllEntitiesForType(final Class<?> clazz) {
		if (clazz.equals(Individual.class)) {
			return individuals;
		}

		if (clazz.equals(Item.class)) {
			return items;
		}

		if (clazz.equals(Prop.class)) {
			return props;
		}

		if (clazz.equals(FluidColumn.class)) {
			return fluidColumns;
		}

		throw new RuntimeException("Unrecognised class: " + clazz.getSimpleName());
	}


	public void removeIndividual(final int key) {
		individuals.remove(key);
	}


	public void removeProp(final int key) {
		props.remove(key);
	}


	public void removeItem(final int key) {
		items.remove(key);
	}


	public void removeFluidColumn(final int key) {
		items.remove(key);
	}


	public void addIndividual(final int key) {
		individuals.add(key);
	}


	public void addProp(final int key) {
		props.add(key);
	}


	public void addItem(final int key) {
		items.add(key);
	}


	public void addFluidColumn(final int key) {
		fluidColumns.add(key);
	}


	public void clear() {
		props.clear();
		individuals.clear();
		items.clear();
		fluidColumns.clear();
	}
}