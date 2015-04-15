package bloodandmithril.graphics.particles;

import bloodandmithril.graphics.WorldRenderer.Depth;
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
	private static final long serialVersionUID = -2839758227577685427L;

	protected long diminishingDuration;
	private float originalRadius, originalGlowIntensity;

	/**
	 * Constructor
	 */
	public DiminishingTracerParticle(
			Vector2 position,
			Vector2 velocity,
			Color color,
			Color glowColor,
			float radius,
			int worldId,
			float glowIntensity,
			MovementMode movementMode,
			Depth depth,
			long diminishingDuration) {
		super(position, velocity, color, glowColor, radius, worldId, new Countdown(diminishingDuration), glowIntensity, movementMode, depth);
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