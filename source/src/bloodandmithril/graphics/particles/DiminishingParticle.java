package bloodandmithril.graphics.particles;

import bloodandmithril.world.Domain.Depth;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * A {@link DiminishingTracerParticle} that does not render the tracer
 *
 * @author Matt
 */
public class DiminishingParticle extends DiminishingTracerParticle {
	private static final long serialVersionUID = 4485079922391112334L;


	/**
	 * Constructor
	 */
	public DiminishingParticle(
			Vector2 position,
			Vector2 velocity,
			Color color,
			float radius,
			int worldId,
			float glowIntensity,
			MovementMode movementMode,
			Depth depth,
			long diminishingDuration) {
		super(position, velocity, color, radius, worldId, glowIntensity, movementMode, depth, diminishingDuration);
	}

	
	@Override
	public synchronized void renderLine(float delta) {
		// Do nothing
	}
}