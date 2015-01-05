package bloodandmithril.graphics.particles;

import bloodandmithril.util.Countdown;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * A {@link TracerParticle} that reduces in size over time
 *
 * @author Matt
 */
public class DiminishingTracerParticle extends TracerParticle {
	private long diminishingDuration;
	float originalRadius, originalGlowIntensity;

	/**
	 * Constructor
	 */
	public DiminishingTracerParticle(
			Vector2 position,
			Vector2 velocity,
			Color color,
			float radius,
			int worldId,
			float glowIntensity,
			MovementMode movementMode,
			boolean background,
			long diminishingDuration) {
		super(position, velocity, color, radius, worldId, new Countdown(diminishingDuration), glowIntensity, movementMode, background);
		this.originalRadius = radius;
		this.originalGlowIntensity = glowIntensity;
		this.diminishingDuration = diminishingDuration;
	}


	@Override
	public synchronized void update(float delta) throws NoTileFoundException {
		float step = diminishingDuration / 1000f;

		radius -= originalRadius * (delta / step);
		glowIntensity -= originalGlowIntensity * (delta / step);

		super.update(delta);
	}
}