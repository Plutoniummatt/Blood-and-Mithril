package bloodandmithril.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.background.BackgroundImages;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.performance.PositionalIndexMap;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

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

	/** Background images of this world */
	private BackgroundImages 									backgroundImages 		= new BackgroundImages();

	/** Gravity on this world */
	private float gravity;

	/** {@link Topography} of this {@link World} */
	private transient Topography topography;

	/** Individuals that are currently in this {@link World} */
	private Set<Integer> 										individuals 			= Sets.newHashSet();

	/** The positional indexing map of this {@link World} */
	private PositionalIndexMap 									positionalIndexMap;

	/** Particles on this {@link World} */
	private transient Collection<Particle> 						clientParticles			= new ConcurrentLinkedDeque<>();

	/** Particles on this {@link World}, this one is saved, and synchronized with server */
	private ConcurrentHashMap<Long, Particle> 					serverParticles			= new ConcurrentHashMap<>();

	/** The items of this {@link World} */
	private WorldItems items;

	/** The props of this {@link World} */
	private WorldProps props;

	/** The projectiles of this {@link World} */
	private WorldProjectiles projectiles;

	/** Epoch of this world */
	private Epoch epoch;

	/**
	 * Constructor
	 */
	public World(float gravity, Epoch epoch) {
		this.epoch = epoch;
		this.worldId = ParameterPersistenceService.getParameters().getNextWorldKey();
		this.gravity = gravity;
		this.items = new WorldItems(worldId);
		this.props = new WorldProps(worldId);
		this.projectiles = new WorldProjectiles(worldId);
		this.topography = new Topography(worldId);
		this.positionalIndexMap = new PositionalIndexMap(worldId);
	}


	public void update() {
		float d = 1f/60f;

		epoch.incrementTime(d);

		for (int individualId : individuals) {
			Domain.getIndividual(individualId).update(d);
		}

		for (Prop prop : props().getProps()) {
			prop.update(d);
		}

		for (Projectile projectile : projectiles().getProjectiles()) {
			projectile.update(d);
		}

		for (Item item : items().getItems()) {
			try {
				item.update(d);
			} catch (NoTileFoundException e) {}
		}
	}


	public Epoch getEpoch() {
		return epoch;
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
	 * @return the {@link WorldProjectiles}
	 */
	public WorldProjectiles projectiles() {
		return projectiles;
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
	public Collection<Particle> getClientParticles() {
		if (clientParticles == null) {
			clientParticles = new ConcurrentLinkedDeque<>();
		}
		return clientParticles;
	}


	/**
	 * @return The transient collection of particles
	 */
	public ConcurrentHashMap<Long, Particle> getServerParticles() {
		if (serverParticles == null) {
			serverParticles = new ConcurrentHashMap<>();
		}
		return serverParticles;
	}


	/**
	 * @return the positional index map of this {@link World}
	 */
	public PositionalIndexMap getPositionalIndexMap() {
		return positionalIndexMap;
	}


	public BackgroundImages getBackgroundImages() {
		return backgroundImages;
	}


	public void setEpoch(Epoch currentEpoch) {
		this.epoch = currentEpoch;
	}
}