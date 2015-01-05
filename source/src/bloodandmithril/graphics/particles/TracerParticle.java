package bloodandmithril.graphics.particles;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
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
public class TracerParticle extends Particle {

	public float glowIntensity;
	public Vector2 prevPosition;

	public TracerParticle(Vector2 position, Vector2 velocity, Color color, float radius, int worldId, SerializableFunction<Boolean> removalCondition, float glowIntensity, MovementMode movementMode, boolean background) {
		super(position, velocity, color, radius, worldId, removalCondition, movementMode, background);
		this.prevPosition = position.cpy();
		this.glowIntensity = glowIntensity;
	}


	@Override
	public synchronized void render(float delta) {
		Topography topography = Domain.getWorld(worldId).getTopography();
		if (topography.hasTile(position.x, position.y, true)) {
			try {
				if (topography.getTile(position.x, position.y, true).isPassable()) {
					Domain.shapeRenderer.setColor(color);
					Domain.shapeRenderer.filledCircle(position.x, position.y, radius <= 0.01f ? 0.01f : radius);
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
					Domain.shapeRenderer.setColor(color);
					Domain.shapeRenderer.line(position.x, position.y, prevPosition.x, prevPosition.y);
				}
			} catch (NoTileFoundException e) {}
		}
	}
}