package bloodandmithril.graphics;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static java.lang.Math.abs;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * A particle that renders like a tracer, the length scales linearly with the velocity of the particle.
 *
 * @author Matt
 */
public class TracerParticle {

	public final float lightRadius;
	public final int worldId;
	public SerializableColor color;
	private Vector2 position, velocity;
	private SerializableFunction<Boolean> removalCondition;

	public TracerParticle(Vector2 position, Vector2 velocity, Color color, float radius, int worldId, SerializableFunction<Boolean> removalCondition) {
		this.position = position;
		this.velocity = velocity;
		this.color = new SerializableColor(color);
		this.lightRadius = radius;
		this.worldId = worldId;
		this.removalCondition = removalCondition;
	}


	public void render(float delta) {
		Domain.shapeRenderer.setProjectionMatrix(BloodAndMithrilClient.cam.combined);
		Domain.shapeRenderer.setColor(Color.RED);
		Domain.shapeRenderer.line(position.x, position.y, position.x + velocity.x * delta, position.y + velocity.y * delta);
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

		Tile tileUnder = Domain.getWorld(worldId).getTopography().getTile(position.x, position.y, true);

		if (tileUnder.isPlatformTile || !tileUnder.isPassable()) {
			Vector2 trial = position.cpy();
			trial.y += -previousVelocity.y*delta;

			if (Domain.getWorld(worldId).getTopography().getTile(trial.x, trial.y, true).isPassable()) {
				if (previousVelocity.y <= 0f && previousVelocity.y != 0f) {
					velocity.x = velocity.x * 0.3f;
					velocity.y = 0f;
					position.y = Domain.getWorld(worldId).getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(position, true).y;
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