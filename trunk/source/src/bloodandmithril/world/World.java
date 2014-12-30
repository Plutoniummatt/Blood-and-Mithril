package bloodandmithril.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.performance.PositionalIndexMap;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.topography.Topography;

import com.google.common.collect.Sets;

/**
 * A World, holds info about the {@link Topography}, {@link Prop}s, {@link Individual}s and any related entities.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
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

	/** The items of this {@link World} */
	private WorldItems items;

	/** The positional indexing map of this {@link World} */
	private PositionalIndexMap positionalIndexMap		= new PositionalIndexMap();

	/** Particles on this {@link World} */
	private transient Collection<Particle> particles	= new ConcurrentLinkedDeque<>();

	/**
	 * Constructor
	 */
	public World(float gravity) {
		this.worldId = ParameterPersistenceService.getParameters().getNextWorldKey();
		this.setGravity(gravity);
		this.items = new WorldItems(worldId);
		Domain.addTopography(worldId, new Topography(worldId));
	}


	/**
	 * @return the {@link #topography}
	 */
	public Topography getTopography() {
		return Domain.getTopography(worldId);
	}


	public WorldItems items() {
		return items;
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


	public Collection<Particle> getParticles() {
		return particles;
	}


	public void setParticles(Collection<Particle> particles) {
		this.particles = particles;
	}


	public PositionalIndexMap getPositionalIndexMap() {
		return positionalIndexMap;
	}
}