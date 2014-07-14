package bloodandmithril.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.graphics.TracerParticle;
import bloodandmithril.item.items.Item;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.topography.Topography;

import com.google.common.collect.Sets;

/**
 * A World, holds info about the {@link Topography}, {@link Prop}s, {@link Individual}s and any related entities.
 *
 * @author Matt
 */
public class World implements Serializable {
	private static final long serialVersionUID = -739119449225954631L;

	/** Unique identifier of this {@link World} */
	private final int worldId;

	/** Gravity on this world */
	private float gravity;

	/** Individuals that are currently in this {@link World} */
	private Set<Integer> individuals 					= Sets.newHashSet();

	/** {@link Prop}s that are on this {@link World} */
	private Set<Integer> props 							= Sets.newHashSet();

	/** {@link Light}s that are present on this {@link World} */
	private Set<Integer> lights 						= Sets.newHashSet();

	/** {@link Item}s that are present on this {@link World} */
	private Set<Integer> items							= Sets.newHashSet();

	/** Particles on this {@link World} */
	private Collection<TracerParticle> particles			= new ConcurrentLinkedDeque<>();

	/**
	 * Constructor
	 */
	public World(float gravity) {
		this.worldId = ParameterPersistenceService.getParameters().getNextWorldKey();
		this.setGravity(gravity);
		Domain.addTopography(worldId, new Topography(worldId));
	}


	/**
	 * @return the {@link #topography}
	 */
	public Topography getTopography() {
		return Domain.getTopography(worldId);
	}


	public float getGravity() {
		return gravity;
	}


	public void setGravity(float gravity) {
		this.gravity = gravity;
	}


	public int getWorldId() {
		return worldId;
	}


	public Set<Integer> getIndividuals() {
		return individuals;
	}


	public Set<Integer> getProps() {
		return props;
	}


	public Set<Integer> getLights() {
		return lights;
	}


	public Set<Integer> getItems() {
		return items;
	}


	public Collection<TracerParticle> getParticles() {
		return particles;
	}
}