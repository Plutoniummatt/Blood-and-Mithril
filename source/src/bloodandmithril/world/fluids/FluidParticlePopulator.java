package bloodandmithril.world.fluids;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.World;

/**
 * @author Sam
 *
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class FluidParticlePopulator {

	/**
	 * @param position
	 * @param velocity
	 * @param volume
	 * @param world
	 * @return a {@link FluidParticle} with the parameters specified.
	 */
	public FluidParticle createFluidParticle(Vector2 position, Vector2 velocity, float volume, World world) {
		FluidParticle particle = new FluidParticle(
			position,
			velocity,
			volume,
			world.getWorldId()
		);
		world.fluids().addFluidParticle(particle);
		return particle;
	}
}
