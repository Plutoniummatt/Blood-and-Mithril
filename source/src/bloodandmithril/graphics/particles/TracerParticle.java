package bloodandmithril.graphics.particles;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * A particle that renders like a tracer, the length scales linearly with the velocity of the particle.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class TracerParticle extends Particle {

	public final float radius;
	public float glowIntensity;
	public Vector2 prevPosition;

	public TracerParticle(Vector2 position, Vector2 velocity, Color color, float radius, int worldId, SerializableFunction<Boolean> removalCondition, float glowIntensity, MovementMode movementMode) {
		super(position, velocity, color, radius, worldId, removalCondition, movementMode);
		this.prevPosition = position.cpy();
		this.radius = radius;
		this.glowIntensity = glowIntensity;
	}


	@Override
	public void render(float delta) {
		Domain.shapeRenderer.setColor(color);
		Domain.shapeRenderer.filledCircle(position.x, position.y, radius);
	}


	@Override
	public void update(float delta) {
		prevPosition.x = position.x;
		prevPosition.y = position.y;

		super.update(delta);
	}


	@Override
	public void renderLine(float delta) {
		Domain.shapeRenderer.setColor(color);
		Domain.shapeRenderer.line(position.x, position.y, prevPosition.x, prevPosition.y);
	}
}