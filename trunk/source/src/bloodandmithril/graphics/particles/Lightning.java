package bloodandmithril.graphics.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

public class Lightning {
	
	private Particle currentParticle;

	/**
	 * Constructor, takes in a world coordinate
	 */
	public Lightning(Vector2 position, int worldId) {
		TracerParticle tracerParticle = new TracerParticle(
			position, 
			new Vector2(
				(Util.getRandom().nextFloat() - 0.5f) * 300f,
				-Util.getRandom().nextFloat() * 500f - 6000f
			), 
			Color.WHITE, 
			Color.WHITE, 
			3f, 
			worldId, 
			new Countdown(500), 
			10f, 
			MovementMode.EMBER, 
			Depth.FOREGROUND
		);
		
		currentParticle = tracerParticle;
		
		Domain.getWorld(worldId).getClientParticles().add(
			tracerParticle
		);
	}
	
	
	public void update() {
		if (currentParticle.getRemovalCondition().call()) {
			currentParticle.velocity = new Vector2(
				(Util.getRandom().nextFloat() - 0.5f) * 300f,
				-Util.getRandom().nextFloat() * 500f - 6000f
			);
			
			Domain.getWorld(currentParticle.worldId).getClientParticles().remove(
				currentParticle
			);
			Domain.getWorld(currentParticle.worldId).getClientParticles().add(
				currentParticle
			);
		}
	}
}