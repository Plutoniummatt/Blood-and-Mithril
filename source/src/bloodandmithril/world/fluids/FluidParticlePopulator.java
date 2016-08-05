package bloodandmithril.world.fluids;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.world.World;

@Singleton
public class FluidParticlePopulator {

	public FluidParticle createFluidParticle(Vector2 position, Vector2 velocity, float radius, World world) {
		FluidParticle particle = new FluidParticle(
			position,
			velocity,
			radius,
			world.getWorldId()
		);
		world.fluids().addFluidParticle(particle);
		return particle;
	}
	
}
