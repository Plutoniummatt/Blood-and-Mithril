package bloodandmithril.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualUpdateService;
import bloodandmithril.core.Copyright;
import bloodandmithril.event.Event;
import bloodandmithril.generation.ChunkGenerator;
import bloodandmithril.graphics.background.BackgroundImages;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.performance.PositionalIndexMap;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A World, holds info about the {@link Topography}, {@link Prop}s, {@link Individual}s and any related entities.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public final class World implements Serializable {
	private static final long serialVersionUID = -739119449225954631L;

	/** Unique identifier of this {@link World} */
	private final int worldId;

	/** Background images of this world */
	private final BackgroundImages 								backgroundImages 		= new BackgroundImages();

	/** Gravity on this world */
	private final float gravity;

	/** {@link Topography} of this {@link World} */
	private transient Topography topography;

	/** Individuals that are currently in this {@link World} */
	private final ConcurrentSkipListSet<Integer>				individuals 			= new ConcurrentSkipListSet<Integer>();

	/** The positional indexing map of this {@link World} */
	private PositionalIndexMap 									positionalIndexMap;

	/** Particles on this {@link World} */
	private transient Collection<Particle> 						clientParticles			= new ConcurrentLinkedDeque<>();
	
	/** Particles on this {@link World}, this one is saved, and synchronized with server */
	private ConcurrentHashMap<Long, Particle> 					serverParticles			= new ConcurrentHashMap<>();

	/** The items of this {@link World} */
	private final WorldItems items;

	/** The props of this {@link World} */
	private final WorldProps props;

	/** The projectiles of this {@link World} */
	private final WorldProjectiles projectiles;

	/** World-specific {@link ChunkGenerator} */
	private final ChunkGenerator 								generator;

	/** Epoch of this world */
	private Epoch epoch;

	/** Time between each update tick */
	private float												updateTick = 1f/60f;

	/** Outstanding events to be processed */
	private final ConcurrentLinkedDeque<Event>					events					= new ConcurrentLinkedDeque<>();

	/**
	 * Constructor
	 */
	public World(float gravity, Epoch epoch, ChunkGenerator generator) {
		this(gravity, epoch, generator, ParameterPersistenceService.getParameters().getNextWorldKey());
	}


	/**
	 * Constructor
	 */
	public World(float gravity, Epoch epoch, ChunkGenerator generator, int worldId) {
		this.epoch = epoch;
		this.generator = generator;
		this.worldId = worldId;
		this.gravity = gravity;
		this.items = new WorldItems(worldId);
		this.props = new WorldProps(worldId);
		this.projectiles = new WorldProjectiles(worldId);
		this.topography = new Topography(worldId);
		this.positionalIndexMap = new PositionalIndexMap(worldId);
	}


	/**
	 * Adds an event
	 */
	public synchronized final void addEvent(Event e) {
		events.add(e);
	}


	/**
	 * Returns events
	 */
	public synchronized final ConcurrentLinkedDeque<Event> getEvents() {
		return events;
	}


	public final World setUpdateTick(float updateTick) {
		this.updateTick = updateTick;
		return this;
	}


	public final void update() {
		epoch.incrementTime(updateTick);

		for (int individualId : individuals) {
			IndividualUpdateService.update(Domain.getIndividual(individualId), updateTick);
		}

		for (Prop prop : props().getProps()) {
			prop.update(updateTick);
		}

		for (Projectile projectile : projectiles().getProjectiles()) {
			projectile.update(updateTick);
		}

		for (Item item : items().getItems()) {
			try {
				item.update(updateTick);
			} catch (NoTileFoundException e) {}
		}
	}


	public final Epoch getEpoch() {
		return epoch;
	}


	/**
	 * @return the {@link #topography}
	 */
	public final Topography getTopography() {
		return topography;
	}


	/**
	 * @return the {@link #topography}
	 */
	public final void setTopography(Topography topography) {
		this.topography = topography;
	}


	/**
	 * @return The {@link WorldItems}
	 */
	public final WorldItems items() {
		return items;
	}


	/**
	 * @return the {@link WorldProps}
	 */
	public final WorldProps props() {
		return props;
	}


	/**
	 * @return the {@link WorldProjectiles}
	 */
	public final WorldProjectiles projectiles() {
		return projectiles;
	}


	/**
	 * @return the gravity of this world
	 */
	public final float getGravity() {
		return gravity;
	}


	/**
	 * @return the world id
	 */
	public final int getWorldId() {
		return worldId;
	}


	public final Set<Integer> getIndividuals() {
		return individuals;
	}


	/**
	 * @return The transient collection of particles
	 */
	public final Collection<Particle> getClientParticles() {
		if (clientParticles == null) {
			clientParticles = new ConcurrentLinkedDeque<>();
		}
		return clientParticles;
	}


	/**
	 * @return The transient collection of particles
	 */
	public final ConcurrentHashMap<Long, Particle> getServerParticles() {
		if (serverParticles == null) {
			serverParticles = new ConcurrentHashMap<>();
		}
		return serverParticles;
	}


	/**
	 * @return the positional index map of this {@link World}
	 */
	public final PositionalIndexMap getPositionalIndexMap() {
		return positionalIndexMap;
	}


	public final BackgroundImages getBackgroundImages() {
		return backgroundImages;
	}


	public final void setEpoch(Epoch currentEpoch) {
		this.epoch = currentEpoch;
	}


	public final ChunkGenerator getGenerator() {
		return generator;
	}
}