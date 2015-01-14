package bloodandmithril.networking.requests;

import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.networking.Response;
import bloodandmithril.world.Domain;

/**
 * {@link Response} object to synchronize server particles
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class SynchronizeParticles implements Response {

	private final int worldId;
	private final ConcurrentHashMap<Long, Particle> particles;

	/**
	 * Constructor
	 */
	public SynchronizeParticles(int worldId) {
		this.worldId = worldId;
		particles = Domain.getWorld(worldId).getServerParticles();
	}


	@Override
	public void acknowledge() {
		Domain.getWorld(worldId).getServerParticles().clear();
		Domain.getWorld(worldId).getServerParticles().putAll(particles);
	}


	@Override
	public int forClient() {
		return -1;
	}


	@Override
	public void prepare() {
	}
}