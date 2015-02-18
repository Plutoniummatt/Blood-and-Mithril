package bloodandmithril.graphics.particles;

import bloodandmithril.world.Domain.Depth;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * A {@link DiminishingTracerParticle} that does not render the tracer, and changes color
 *
 * @author Matt
 */
public class DiminishingColorChangingParticle extends DiminishingTracerParticle {
	private static final long serialVersionUID = 4485079922391112334L;
	private float rStep = 0f;
	private float gStep = 0f;
	private float bStep = 0f;
	private float aStep = 0f;

	/**
	 * Constructor
	 */
	public DiminishingColorChangingParticle(
			Vector2 position,
			Vector2 velocity,
			Color color,
			Color colorToChangeTo,
			float radius,
			int worldId,
			float glowIntensity,
			MovementMode movementMode,
			Depth depth,
			long diminishingDuration) {
		super(position, velocity, color, radius, worldId, glowIntensity, movementMode, depth, diminishingDuration);

		if (colorToChangeTo != null) {
			this.rStep = colorToChangeTo.r - color.r;
			this.gStep = colorToChangeTo.g - color.g;
			this.bStep = colorToChangeTo.b - color.b;
			this.aStep = colorToChangeTo.a - color.a;
		}
	}


	@Override
	public synchronized void update(float delta) throws NoTileFoundException {
		super.update(delta);
		float step = diminishingDuration / 1000f;

		color.r += rStep * (delta / step);
		color.g += gStep * (delta / step);
		color.b += bStep * (delta / step);
		color.a += aStep * (delta / step);
	}


	@Override
	public synchronized void renderLine(float delta) {
		// Do nothing
	}
}