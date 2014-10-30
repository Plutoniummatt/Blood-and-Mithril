package bloodandmithril.graphics.particles;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static java.lang.Math.abs;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

@Copyright("Matthew Peck 2014")
public abstract class Particle {

	public final int worldId;

	public Color color;
	public Vector2 position, velocity;
	public float radius;
	private SerializableFunction<Boolean> removalCondition;
	private MovementMode movementMode = MovementMode.GRAVITY;

	/**
	 * Constructor
	 */
	protected Particle(Vector2 position, Vector2 velocity, Color color, float radius, int worldId, SerializableFunction<Boolean> removalCondition, MovementMode movementMode) {
		this.position = position;
		this.velocity = velocity;
		this.radius = radius;
		this.color = color;
		this.worldId = worldId;
		this.removalCondition = removalCondition;
		this.movementMode = movementMode;
	}

	/**
	 * Renders the point of this particle
	 */
	public abstract void render(float delta);

	/**
	 * Renders the movement line, if required
	 */
	public abstract void renderLine(float delta);

	/**
	 * Performs kinetics updates on this particle
	 */
	public void update(float delta) {
		Vector2 previousPosition = position.cpy();
		Vector2 previousVelocity = velocity.cpy();

		position.add(velocity.cpy().mul(delta));

		movement(delta);
		
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

	
	private void movement(float delta) {
		switch (movementMode) {
		case GRAVITY:
			gravitational(delta);
			break;
		case EMBER:
			ember(delta);
			break;
		default:
		}
	}

	
	private void ember(float delta) {
		velocity.y = velocity.y + delta * 150f;
		velocity.mul(0.98f);
	}

	
	private void gravitational(float delta) {
		float gravity = Domain.getWorld(worldId).getGravity();
		if (abs((velocity.y - gravity * delta) * delta) < TILE_SIZE/2) {
			velocity.y = velocity.y - delta * gravity;
		} else {
			velocity.y = velocity.y * 0.8f;
		}
		velocity.mul(0.98f);
	}


	public SerializableFunction<Boolean> getRemovalCondition() {
		return removalCondition;
	}
	
	
	public enum MovementMode {
		GRAVITY, EMBER
	}
}