package bloodandmithril.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.common.collect.Lists;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.event.Event;
import bloodandmithril.generation.biome.BiomeDecider;
import bloodandmithril.graphics.background.BackgroundImages;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.performance.PositionalIndexMap;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.weather.Cloud;
import bloodandmithril.world.weather.WeatherState;

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

	/** Gravity on this world */
	private final float gravity;

	/** {@link Topography} of this {@link World} */
	private transient Topography topography;

	/** Background images of this world */
	private final BackgroundImages 								backgroundImages 		= new BackgroundImages();

	/** Individuals that are currently in this {@link World} */
	private final ConcurrentSkipListSet<Integer>				individuals 			= new ConcurrentSkipListSet<>();

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

	/** The fluids of this {@link World} */
	private final WorldFluids fluids;

	/** Epoch of this world */
	private Epoch epoch;

	/** Outstanding events to be processed */
	private final ConcurrentLinkedDeque<Event>					events					= new ConcurrentLinkedDeque<>();

	/** Biome decider of this {@link World} */
	private final Class<? extends BiomeDecider> 				biomeDecider;

	/** {@link Cloud}s on this world */
	private final List<Cloud>	 								clouds					= Lists.newLinkedList();

	/** The {@link WeatherState} of this world */
	private final WeatherState 									weatherState			= new WeatherState();

	/**
	 * Constructor
	 */
	public World(final float gravity, final Epoch epoch, final Class<? extends BiomeDecider> biomeDecider) {
		this(gravity, epoch, Wiring.injector().getInstance(ParameterPersistenceService.class).getParameters().getNextWorldKey(), biomeDecider);
	}


	/**
	 * Constructor
	 */
	public World(final float gravity, final Epoch epoch, final int worldId, final Class<? extends BiomeDecider> biomeDecider) {
		this.epoch = epoch;
		this.worldId = worldId;
		this.gravity = gravity;
		this.biomeDecider = biomeDecider;
		this.items = new WorldItems(worldId);
		this.props = new WorldProps(worldId);
		this.projectiles = new WorldProjectiles(worldId);
		this.fluids = new WorldFluids(worldId);
		this.topography = new Topography();
		this.positionalIndexMap = new PositionalIndexMap(worldId);

		clouds.add(new Cloud(1, 0, -300));
		clouds.add(new Cloud(2, 0, 0));
		clouds.add(new Cloud(3, 0, 300));
	}


	/**
	 * Adds an event
	 */
	public synchronized final void addEvent(final Event e) {
		events.add(e);
	}


	/**
	 * Returns events
	 */
	public synchronized final ConcurrentLinkedDeque<Event> getEvents() {
		return events;
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
	public final void setTopography(final Topography topography) {
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


	public final void setEpoch(final Epoch currentEpoch) {
		this.epoch = currentEpoch;
	}


	/**
	 * @return the {@link BiomeDecider} implementation for this {@link World}
	 */
	public Class<? extends BiomeDecider> getBiomeDecider() {
		return biomeDecider;
	}


	public List<Cloud> getClouds() {
		return clouds;
	}


	public WeatherState getWeatherState() {
		return weatherState;
	}


	public WorldFluids fluids() {
		return fluids;
	}
}