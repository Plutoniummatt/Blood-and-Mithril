package bloodandmithril.graphics.particles;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import java.io.Serializable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

@Copyright("Matthew Peck 2014")
public abstract class Particle implements Serializable {
	private static final long serialVersionUID = 3353509929496197072L;

	public final int worldId;

	public final long particleId;
	public SerializableColor color;
	public Vector2 position, velocity;
	public float radius;
	public final Depth depth;
	protected boolean doNotUpdate;
	private SerializableFunction<Boolean> removalCondition;
	private MovementMode movementMode = MovementMode.GRAVITY;
	private boolean bounces = false;

	/**
	 * Constructor
	 */
	protected Particle(Vector2 position, Vector2 velocity, Color color, float radius, int worldId, SerializableFunction<Boolean> removalCondition, MovementMode movementMode, Depth depth) {
		this.position = position;
		this.velocity = velocity;
		this.radius = radius;
		this.color = new SerializableColor(color);
		this.worldId = worldId;
		this.removalCondition = removalCondition;
		this.movementMode = movementMode;
		this.depth = depth;
		this.particleId = ParameterPersistenceService.getParameters().getNextParticleId();
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
	 * Instruct this particle to bounce
	 */
	public Particle bounce() {
		this.bounces = true;
		return this;
	}


	/**
	 * Instructs this particle to not move
	 */
	public void doNotUpdate() {
		doNotUpdate = true;
	}

	/**
	 * Performs kinetics updates on this particle
	 */
	public synchronized void update(float delta) throws NoTileFoundException {
		if (doNotUpdate) {
			return;
		}

		Vector2 previousPosition = position.cpy();
		Vector2 previousVelocity = velocity.cpy();

		position.add(velocity.cpy().scl(0.016f));

		movement(delta);

		Tile tileUnder = Domain.getWorld(worldId).getTopography().getTile(position.x, position.y, true);
		if (tileUnder.isPlatformTile || !tileUnder.isPassable()) {
			Vector2 trial = position.cpy();
			trial.y += -previousVelocity.y*delta;

			if (Domain.getWorld(worldId).getTopography().getTile(trial.x, trial.y, true).isPassable()) {
				if (previousVelocity.y <= 0f && previousVelocity.y != 0f && !bounces) {
					velocity.x = velocity.x * 0.6f;
					velocity.y = 0f;
					position.y = Topography.convertToWorldCoord(Topography.convertToWorldTileCoord(position.y), true) + TILE_SIZE;
				} else {
					position = previousPosition;
					velocity.y = -previousVelocity.y * 0.5f;
				}
			} else {
				velocity.x = -velocity.x * 0.1f;
				position = previousPosition;
			}
		}
	}


	private void movement(float delta) throws NoTileFoundException {
		switch (movementMode) {
		case GRAVITY:
			gravitational(delta);
			break;
		case EMBER:
			ember(delta);
			break;
		case WEIGHTLESS:
			weightless(delta);
			break;
		default:
		}
	}


	private void weightless(float delta) {
		velocity.scl(0.98f);
	}


	private void ember(float delta) {
		velocity.y = velocity.y + delta * 150f;
		velocity.scl(0.98f);
	}


	private void gravitational(float delta) throws NoTileFoundException {
		position.add(velocity.cpy().scl(delta));
		float gravity = Domain.getWorld(worldId).getGravity();
		if (velocity.len() > 2000f) {
			velocity.add(0f, -gravity * delta).scl(0.95f);
		} else {
			velocity.add(0f, -gravity * delta);
		}

		Tile tile = Domain.getWorld(worldId).getTopography().getTile(position.x, position.y - 1, true);
		if (tile.isPlatformTile || !tile.isPassable()) {
			velocity.y = 0f;
			velocity.x = velocity.x * 0.6f;
		}
	}


	public SerializableFunction<Boolean> getRemovalCondition() {
		return removalCondition;
	}


	public enum MovementMode implements Serializable {
		GRAVITY, EMBER, WEIGHTLESS
	}
}