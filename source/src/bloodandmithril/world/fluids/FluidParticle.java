package bloodandmithril.world.fluids;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.world.topography.Topography;

/**
 * Represents a fluid particle
 * 
 * @author Sam
 */
@Copyright("Matthew Peck 2016")
public class FluidParticle implements Serializable {
	private static final long serialVersionUID = -8620078003575928127L;

	public final Vector2 position, velocity;
	public final int worldId;
	public final long id;
	
	private float volume;
	private float radius;

	
	/**
	 * Constructor
	 */
	public FluidParticle(Vector2 position, Vector2 velocity, float volume, int worldId) {
		this.position = position;
		this.velocity = velocity;
		this.setVolume(volume);
		this.setRadius((float)(Math.sqrt(Math.pow(Topography.TILE_SIZE,2) * volume / Math.PI)));
		this.worldId = worldId;
		this.id = Wiring.injector().getInstance(ParameterPersistenceService.class).getParameters().getNextFluidParticleId();
	}

	
	public float getRadius() {
		return radius;
	}

	
	public void setRadius(float radius) {
		this.radius = radius;
	}


	public float getVolume() {
		return volume;
	}


	public void setVolume(float volume) {
		this.volume = volume;
	}
}