package bloodandmithril.graphics.particles;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Performance;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * A particle that renders like a tracer, the length scales linearly with the velocity of the particle.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Performance(explanation = "Use with care, these don't perform as well as we hope")
public class TracerParticle extends Particle {
	private static final long serialVersionUID = 4646397525209769227L;

	public float glowIntensity;
	public Vector2 prevPosition;
	public SerializableColor glowColow;

	public TracerParticle(Vector2 position, Vector2 velocity, Color color, Color glowColor, float radius, int worldId, SerializableFunction<Boolean> removalCondition, float glowIntensity, MovementMode movementMode, Depth depth) {
		super(position, velocity, color, radius, worldId, removalCondition, movementMode, depth);
		this.prevPosition = position.cpy();
		this.glowIntensity = glowIntensity;
		this.glowColow = new SerializableColor(glowColor);
	}


	@Override
	public synchronized void render(float delta) {
		Topography topography = Domain.getWorld(worldId).getTopography();
		if (topography.hasTile(position.x, position.y, true)) {
			try {
				if (topography.getTile(position.x, position.y, true).isPassable()) {
					Domain.shapeRenderer.setColor(color.getColor());
					Domain.shapeRenderer.filledCircle(position.x, position.y, radius <= 0.05f ? 0.05f : radius);
				}
			} catch (NoTileFoundException e) {}
		}
	}


	@Override
	public synchronized void update(float delta) throws NoTileFoundException {
		if (doNotUpdate) {
			return;
		}
		prevPosition.x = position.x;
		prevPosition.y = position.y;

		super.update(delta);
	}


	@Override
	public synchronized void renderLine(float delta) {
		Topography topography = Domain.getWorld(worldId).getTopography();
		if (topography.hasTile(position.x, position.y, true) && topography.hasTile(prevPosition.x, prevPosition.y, true)) {
			try {
				if (topography.getTile(position.x, position.y, true).isPassable()) {
					Gdx.gl.glLineWidth(radius == 1f ? 1f : radius + 1f);
					Domain.shapeRenderer.setColor(color.getColor());
					Domain.shapeRenderer.line(position.x, position.y, prevPosition.x, prevPosition.y);
				}
			} catch (NoTileFoundException e) {}
		}
	}
}