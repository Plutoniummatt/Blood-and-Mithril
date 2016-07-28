package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Individual.Action.JUMP_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.JUMP_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT;
import static bloodandmithril.util.ComparisonUtil.obj;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static java.lang.Math.abs;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.CornerType;

/**
 * The new and improved individual position/velocity updater....
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ImprovedIndividualKinematicsUpdater implements IndividualKinematicsUpdater {

	
	@Override
	public void update(float delta, World world, Individual individual) throws NoTileFoundException {
		Vector2 velocity = individual.getState().velocity;
		Vector2 position = individual.getState().position;
		Vector2 acceleration = individual.getState().acceleration;
		
		Tile surface = world.getTopography().getSurfaceTile(position.x, position.y);
		
		Vector2 surfaceVector = deriveSurfaceVector(position, surface.getCornerType());
		Vector2 surfaceLocation = world.getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(position, true);
		
		preventAccelerationIfMidAir(position, acceleration, surfaceLocation);
		updatePosition(delta, velocity, position);
		gravitation(delta, world, velocity, acceleration);
		
		if (!obj(individual.getCurrentAction()).oneOf(JUMP_LEFT, JUMP_RIGHT)) {
			friction(individual);
		}
		
		if (position.y < surfaceLocation.y) {
			surface = world.getTopography().getSurfaceTile(position.x, position.y);
			surfaceLocation = world.getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(position, true);
			
			int cornerTileOffset = deriveCornerTileOffset(position, surface);
			
			if (obj(individual.getCurrentAction()).oneOf(JUMP_LEFT, JUMP_RIGHT)) {
				if (terminateJump(individual, surfaceVector, velocity)) {
					position.y = surfaceLocation.y - cornerTileOffset;
					velocity.y = 0f;
				}
			} else {
				position.y = surfaceLocation.y - cornerTileOffset;
				velocity.y = 0f;
			}
		}
	}


	private boolean terminateJump(
		Individual individual, 
		Vector2 surfaceVector,
		Vector2 velocity
	) {
		if (velocity.dot(surfaceVector) <= 0) {
			individual.setCurrentAction(velocity.x > 0 ? STAND_RIGHT : STAND_LEFT);
			return true;
		}
		
		return false;
	}


	/**
	 * Derives the effective normal vector of the surface the individual is on
	 */
	private Vector2 deriveSurfaceVector(Vector2 position, CornerType cornerType) {
		Vector2 surfaceVector = new Vector2();
		
		switch (cornerType) {
		case SLOPE_UP:
			surfaceVector = new Vector2(-1f, 1f);
			break;
		case SLOPE_DOWN:
			surfaceVector = new Vector2(1f, 1f);
			break;
		case SLOPE_UP_THEN_DOWN:
			int mod = (int) -position.x % TILE_SIZE;
			if (position.x < 0) {
				surfaceVector = mod <= TILE_SIZE/2 ? new Vector2(2f, 1f) : new Vector2(-2f, 1f);
			} else {
				surfaceVector = mod <= TILE_SIZE/2 ? new Vector2(-2f, 1f) : new Vector2(2f, 1f);
			}
			break;
		case NONE:
			surfaceVector = new Vector2(0f, 1f);
			break;
		}
		
		return surfaceVector;
	}


	/**
	 * Handles corner tiles, e.g. stairs
	 */
	private int deriveCornerTileOffset(Vector2 position, Tile surface) {
		int triangleTileOffset = 0;
		
		switch (surface.getCornerType()) {
		case SLOPE_UP:
			if (position.x < 0) {
				triangleTileOffset = - (int) position.x % TILE_SIZE;
			} else {
				triangleTileOffset = - (int) position.x % TILE_SIZE;
			}
			break;
		case SLOPE_DOWN:
			if (position.x < 0) {
				triangleTileOffset = TILE_SIZE + (int) position.x % TILE_SIZE;
			} else {
				triangleTileOffset = TILE_SIZE + (int) position.x % TILE_SIZE;
			}
			break;
		case SLOPE_UP_THEN_DOWN:
			int mod = (int) -position.x % TILE_SIZE;
			if (position.x < 0) {
				if (mod <= TILE_SIZE/2) {
					triangleTileOffset = TILE_SIZE - 2 * mod;
				} else {
					triangleTileOffset = - TILE_SIZE + 2 * mod;
				}
			} else {
				
			}
			break;
		case NONE:
			break;
		}
		
		return triangleTileOffset;
	}


	/**
	 * Prevents any movement related acceleration if we're in mid air
	 */
	private void preventAccelerationIfMidAir(Vector2 position, Vector2 acceleration, Vector2 surfaceLocation) {
		if (position.y >= surfaceLocation.y + TILE_SIZE/4) {
			acceleration.x = 0f;
		}
	}


	/**
	 * Updates the position based on velocity
	 */
	private void updatePosition(float delta, Vector2 velocity, Vector2 position) {
		final Vector2 distanceTravelled = velocity.cpy().scl(delta);
		position.add(distanceTravelled);
	}

	
	/**
	 * Applies gravitation to the individual
	 */
	private void gravitation(float delta, World world, Vector2 velocity, Vector2 acceleration) {
		if (abs((velocity.y - world.getGravity() * delta) * delta) < TILE_SIZE - 1) {
			velocity.y = velocity.y - delta * world.getGravity();
		} else {
			velocity.y = velocity.y * 0.8f;
		}
		velocity.add(acceleration.cpy().scl(delta));
	}
	
	
	/**
	 * Applies friction to the individual
	 */
	private void friction(final Individual individual) {
		final Controls controls = Wiring.injector().getInstance(Controls.class);
		IndividualState state = individual.getState();

		if (individual.isCommandActive(controls.moveRight.keyCode) && state.velocity.x < 0f ||
			individual.isCommandActive(controls.moveLeft.keyCode) && state.velocity.x > 0f ||
			!individual.isCommandActive(controls.moveLeft.keyCode) && !individual.isCommandActive(controls.moveRight.keyCode)) {

			if (obj(individual.getCurrentAction()).oneOf(Action.JUMP_LEFT, Action.JUMP_RIGHT)) {
				return;
			}

			state.velocity.x = state.velocity.x * 0.3f;
		}
	}
}