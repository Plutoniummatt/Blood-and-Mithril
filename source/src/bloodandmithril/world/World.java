package bloodandmithril.world;

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.Sets;

import bloodandmithril.character.Individual;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.topography.Topography;

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
	private Set<Integer> individuals 	= Sets.newHashSet();
	
	/** {@link Prop}s that are on this {@link World} */
	private Set<Integer> props 			= Sets.newHashSet();
	
	/** {@link Light}s that are present on this {@link World} */
	private Set<Integer> lights 		= Sets.newHashSet();
	
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


	public void setIndividuals(Set<Integer> individuals) {
		this.individuals = individuals;
	}


	public Set<Integer> getProps() {
		return props;
	}


	public void setProps(Set<Integer> props) {
		this.props = props;
	}


	public Set<Integer> getLights() {
		return lights;
	}


	public void setLights(Set<Integer> lights) {
		this.lights = lights;
	}
}