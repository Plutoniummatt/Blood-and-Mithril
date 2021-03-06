package bloodandmithril.graphics.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A {@link DiminishingTracerParticle} that does not render the tracer, and changes color
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class DiminishingColorChangingParticle extends DiminishingTracerParticle {
	private static final long serialVersionUID = 4485079922391112334L;

	private boolean changeColor = false;
	private float rStep = 0f;
	private float gStep = 0f;
	private float bStep = 0f;
	private float aStep = 0f;
	private boolean tracer;

	/**
	 * Constructor
	 */
	public DiminishingColorChangingParticle(
			final Vector2 position,
			final Vector2 velocity,
			final Color color,
			final Color glowColor,
			final Color colorToChangeTo,
			final float radius,
			final int worldId,
			final float glowIntensity,
			final MovementMode movementMode,
			final Depth depth,
			final long diminishingDuration,
			final boolean tracer) {
		super(position, velocity, color, glowColor, radius, worldId, glowIntensity, movementMode, depth, diminishingDuration);
		this.tracer = tracer;

		if (colorToChangeTo != null) {
			this.rStep = colorToChangeTo.r - color.r;
			this.gStep = colorToChangeTo.g - color.g;
			this.bStep = colorToChangeTo.b - color.b;
			this.aStep = colorToChangeTo.a - color.a;
			this.changeColor = true;
		}
	}


	@Override
	public synchronized void update(final float delta) throws NoTileFoundException {
		super.update(delta);
		final float step = diminishingDuration / 1000f;

		if (changeColor) {
			color.r += rStep * (delta / step);
			color.g += gStep * (delta / step);
			color.b += bStep * (delta / step);
			color.a += aStep * (delta / step);
		}
	}


	@Override
	public synchronized void renderLine(final float delta) {
		if (tracer) {
			super.renderLine(delta);
		}
	}
}