package bloodandmithril.graphics;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static java.lang.Math.abs;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * A particle that renders like a tracer, the length scales linearly with the velocity of the particle.
 *
 * @author Matt
 */
public class TracerParticle {

	public final float radius;
	public final int worldId;
	public Color color;
	public float glowIntensity;
	public Vector2 position, velocity;
	private SerializableFunction<Boolean> removalCondition;

	public TracerParticle(Vector2 position, Vector2 velocity, Color color, float radius, int worldId, SerializableFunction<Boolean> removalCondition, float glowIntensity) {
		this.position = position;
		this.velocity = velocity;
		this.color = color;
		this.radius = radius;
		this.worldId = worldId;
		this.removalCondition = removalCondition;
		this.glowIntensity = glowIntensity;
	}


	public void renderPoint(float delta) {
		Domain.shapeRenderer.setColor(color);
		Domain.shapeRenderer.filledCircle(position.x, position.y, radius);
	}


	public void render(float delta) {
		Domain.shapeRenderer.setColor(color);
		Domain.shapeRenderer.line(position.x, position.y, position.x - velocity.x * delta, position.y - velocity.y * delta);
	}


	/**
	 * Performs kinetics updates on this particle
	 */
	public void update(float delta) {
		Vector2 previousPosition = position.cpy();
		Vector2 previousVelocity = velocity.cpy();

		position.add(velocity.cpy().mul(delta));

		float gravity = Domain.getWorld(worldId).getGravity();
		if (abs((velocity.y - gravity * delta) * delta) < TILE_SIZE/2) {
			velocity.y = velocity.y - delta * gravity;
		} else {
			velocity.y = velocity.y * 0.8f;
		}

		velocity.mul(0.98f);
		Tile tileUnder = Domain.getWorld(worldId).getTopography().getTile(position.x, position.y, true);

		if (tileUnder.isPlatformTile || !tileUnder.isPassable()) {
			Vector2 trial = position.cpy();
			trial.y += -previousVelocity.y*delta;

			if (Domain.getWorld(worldId).getTopography().getTile(trial.x, trial.y, true).isPassable()) {
				if (previousVelocity.y <= 0f && previousVelocity.y != 0f) {
					velocity.x = velocity.x * 0.6f;
					velocity.y = 0f;
					position.y = Topography.convertToWorldCoord(Topography.convertToWorldTileCoord(position.y), true) + TILE_SIZE;
				} else {
					position = previousPosition;
					velocity.y = -previousVelocity.y;
				}
			} else {
				velocity.x = 0f;
				position = previousPosition;
			}
		}
	}


	public SerializableFunction<Boolean> getRemovalCondition() {
		return removalCondition;
	}
}