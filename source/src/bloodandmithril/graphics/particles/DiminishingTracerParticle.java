package bloodandmithril.graphics.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.util.Countdown;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A {@link TracerParticle} that reduces in size over time
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class DiminishingTracerParticle extends TracerParticle {
	private static final long serialVersionUID = -2839758227577685427L;

	protected long diminishingDuration;
	private float originalRadius, originalGlowIntensity;

	/**
	 * Constructor
	 */
	public DiminishingTracerParticle(
			final Vector2 position,
			final Vector2 velocity,
			final Color color,
			final Color glowColor,
			final float radius,
			final int worldId,
			final float glowIntensity,
			final MovementMode movementMode,
			final Depth depth,
			final long diminishingDuration) {
		super(position, velocity, color, glowColor, radius, worldId, new Countdown(diminishingDuration), glowIntensity, movementMode, depth);
		this.originalRadius = radius;
		this.originalGlowIntensity = glowIntensity;
		this.diminishingDuration = diminishingDuration;
	}


	@Override
	public synchronized void update(final float delta) throws NoTileFoundException {
		final float step = diminishingDuration / 1000f;

		radius -= originalRadius * (delta / step);
		glowIntensity -= originalGlowIntensity * (delta / step);

		super.update(delta);
	}
}