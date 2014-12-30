package bloodandmithril.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.performance.PositionalIndexMap;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.fluids.FluidBody;
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

	/** {@link Topography} of this {@link World} */
	private transient Topography topography;

	/** Individuals that are currently in this {@link World} */
	private Set<Integer> 										individuals 			= Sets.newHashSet();

	/** All {@link FluidBody}s on this {@link World} */
	private Collection<FluidBody> 								fluids					= new ConcurrentLinkedQueue<>();

	/** The positional indexing map of this {@link World} */
	private PositionalIndexMap 									positionalIndexMap		= new PositionalIndexMap();

	/** Particles on this {@link World} */
	private transient Collection<Particle> 						particles				= new ConcurrentLinkedDeque<>();

	/** The items of this {@link World} */
	private WorldItems items;

	/** The props of this {@link World} */
	private WorldProps props;

	/**
	 * Constructor
	 */
	public World(float gravity) {
		this.worldId = ParameterPersistenceService.getParameters().getNextWorldKey();
		this.gravity = gravity;
		this.items = new WorldItems(worldId);
		this.props = new WorldProps(worldId);
		this.topography = new Topography(worldId);
	}


	/**
	 * @return the {@link #topography}
	 */
	public Topography getTopography() {
		return topography;
	}


	/**
	 * @return the {@link #topography}
	 */
	public void setTopography(Topography topography) {
		this.topography = topography;
	}


	/**
	 * @return The {@link WorldItems}
	 */
	public WorldItems items() {
		return items;
	}


	/**
	 * @return the {@link WorldProps}
	 */
	public WorldProps props() {
		return props;
	}


	/**
	 * @return the gravity of this world
	 */
	public float getGravity() {
		return gravity;
	}


	/**
	 * @return the world id
	 */
	public int getWorldId() {
		return worldId;
	}


	public Set<Integer> getIndividuals() {
		return individuals;
	}


	/**
	 * @return The transient collection of particles
	 */
	public Collection<Particle> getParticles() {
		if (particles == null) {
			particles = new ConcurrentLinkedDeque<>();
		}
		return particles;
	}


	/**
	 * @return the positional index map of this {@link World}
	 */
	public PositionalIndexMap getPositionalIndexMap() {
		return positionalIndexMap;
	}


	/**
	 * Adds a {@link FluidBody} to this {@link World}
	 */
	public void addFluid(FluidBody fluid) {
		fluids.add(fluid);
	}


	/**
	 * Renders the fluids
	 */
	public void renderFluids() {
		fluids.stream().forEach(
			fluid -> {
				fluid.render();
				fluid.renderBindingBox();
			}
		);
	}


	/**
	 * Post-processing of tile deletion, for fluids
	 */
	public void tileDeletion(int worldTileX, int worldTileY) {
		for (FluidBody fluid : fluids) {
			fluid.newSpace(worldTileX, worldTileY);
		}
	}


	/**
	 * Updates this world, delta is in seconds
	 */
	public void updateFluids() {
		for (FluidBody fluid : fluids) {
			fluid.update();
		}
	}
	
	
	public boolean removeFluid(FluidBody fluid) {
		return fluids.remove(fluid);
	}
}