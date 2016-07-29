package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Individual.Action.JUMP_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.JUMP_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT;
import static bloodandmithril.util.ComparisonUtil.obj;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Function;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AIProcessor.JitGoToLocation;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.gotolocation.GoToLocation;
import bloodandmithril.character.ai.task.gotolocation.GoToMovingLocation;
import bloodandmithril.character.ai.task.idle.Idle;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.CornerType;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * The new and improved individual position/velocity updater....
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ImprovedIndividualKinematicsUpdater implements IndividualKinematicsUpdater {
	
	/**
	 * @see bloodandmithril.character.individuals.IndividualKinematicsUpdater#update(float, bloodandmithril.world.World, bloodandmithril.character.individuals.Individual)
	 */
	@Override
	public void update(float delta, World world, Individual individual) throws NoTileFoundException {
		Vector2 velocity = individual.getState().velocity;
		Vector2 position = individual.getState().position;
		Vector2 acceleration = individual.getState().acceleration;
		
		Function<Vector2, Boolean> excludePassable = vector -> {
			try {
				return isTilePassable(vector.x, vector.y, world.getTopography(), individual.getKinematicsData(), individual.getAI());
			} catch (NoTileFoundException e) {
				return false;
			}
		};
		
		Tile surface = world.getTopography().getSurfaceTile(position.x, position.y + individual.getHeight() / 5, excludePassable);
		Vector2 surfaceLocation = world.getTopography().getLowestEmptyTileOrPlatformTileWorldCoordsExludeSpecified(position.x, position.y + individual.getHeight() / 5, true, excludePassable);
		Vector2 surfaceVector = deriveSurfaceVector(position, surface.getCornerType());
		terrainDetection(individual, velocity, position, surface, surfaceVector, surfaceLocation);
		
		surface = world.getTopography().getSurfaceTile(position.x, position.y + individual.getHeight() / 5, excludePassable);
		surfaceLocation = world.getTopography().getLowestEmptyTileOrPlatformTileWorldCoordsExludeSpecified(position.x, position.y + individual.getHeight() / 5, true, excludePassable);
		
		preventAccelerationIfMidAir(position, acceleration, surfaceLocation);
		updatePosition(delta, velocity, position, world.getTopography(), individual);
 		gravitation(delta, world, velocity, acceleration);
		
		if (!isJumping(individual)) {
			friction(individual);
		}
	}


	/**
	 * Applies terrain detection and makes corrections
	 */
	private boolean terrainDetection(
		Individual individual, 
		Vector2 velocity, 
		Vector2 position, 
		Tile surface, 
		Vector2 surfaceVector, 
		Vector2 surfaceLocation
	) {
		if (position.y < surfaceLocation.y) {
			int cornerTileOffset = deriveCornerTileOffset(position, surface);
			
			if (isJumping(individual)) {
				if (terminateJump(individual, surfaceVector, velocity)) {
					position.y = surfaceLocation.y - cornerTileOffset;
					velocity.y = 0f;
					return true;
				}
			// } else if (abs(surfaceLocation.y - cornerTileOffset - position.y) < TILE_SIZE) {
			} else {
				position.y = surfaceLocation.y - cornerTileOffset;
				velocity.y = 0f;
				return true;
			}
		}
		
		return false;
	}


	/**
	 * @param individual
	 * @return true if the given individual is jumping
	 */
	private boolean isJumping(Individual individual) {
		return obj(individual.getCurrentAction()).oneOf(JUMP_LEFT, JUMP_RIGHT);
	}


	/**
	 * @param individual
	 * @param surfaceVector
	 * @param velocity
	 * 
	 * @return true if the jump should be terminated, given the velocity of the individual and the surface normal vector
	 */
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
		int x = round(position.x);
		
		switch (surface.getCornerType()) {
		case SLOPE_UP:
			if (x < -1) {
				triangleTileOffset = - (int) position.x % TILE_SIZE;
			} else {
				triangleTileOffset = TILE_SIZE - (int) position.x % TILE_SIZE;
			}
			break;
		case SLOPE_DOWN:
			if (x < -1) {
				triangleTileOffset = TILE_SIZE + (int) position.x % TILE_SIZE;
			} else {
				triangleTileOffset = (int) position.x % TILE_SIZE;
			}
			break;
		case SLOPE_UP_THEN_DOWN:
			if (x < -1) {
				int mod = (int) -position.x % TILE_SIZE;
				if (mod <= TILE_SIZE/2) {
					triangleTileOffset = TILE_SIZE - 2 * mod;
				} else {
					triangleTileOffset = - TILE_SIZE + 2 * mod;
				}
			} else {
				int mod = (int) position.x % TILE_SIZE;
				if (mod <= TILE_SIZE/2) {
					triangleTileOffset = TILE_SIZE - 2 * mod;
				} else {
					triangleTileOffset = - TILE_SIZE + 2 * mod;
				}
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
	private void updatePosition(float delta, Vector2 velocity, Vector2 position, Topography topography, Individual individual) {
		final Vector2 distanceTravelled = velocity.cpy().scl(delta);
		final Vector2 previousVel = velocity.cpy();

		position.add(distanceTravelled);
		
		boolean invalid = false;
		while (isInvalidPosition(individual, position, topography)) {
			position.x = position.x -= velocity.x * delta;
			position.y = position.y -= velocity.y * delta;
			invalid = true;
		}
		
		if (invalid) {
			position.x = position.x -= velocity.x * delta * 10;
			position.y = position.y -= velocity.y * delta * 10;
			velocity.x = -previousVel.x * 0.4f;
			velocity.x = velocity.x < 0 ? min(-60f, velocity.x) : max(60f, velocity.x);
			velocity.y = 0;
			individual.clearCommands();
			
			if (!isJumping(individual)) {
				individual.getAI().setCurrentTask(new Idle());
			}
			
			return;
		}
	}

	
	/**
	 * @param position
	 * @return whether the individual is at a valid position
	 */
	private boolean isInvalidPosition(Individual individual, Vector2 position, Topography topography) {
		final int blockspan = individual.getHeight()/TILE_SIZE + (individual.getHeight() % TILE_SIZE == 0 ? 0 : 1);
		for (int block = isJumping(individual) ? 0 : 1; block != blockspan; block++) {
			try {
				final Tile tile = topography.getTile(position.x + block, position.y + block * TILE_SIZE + TILE_SIZE / 4, true);
				if ((tile.getCornerType() == CornerType.NONE) && !isTilePassable(
					position.x + block, 
					position.y + block * TILE_SIZE + TILE_SIZE / 4, 
					topography, 
					individual.getKinematicsData(), 
					individual.getAI()
				)) {
					return true;
				}
			} catch (NoTileFoundException e) {
				return true;
			}
		}
		
		return false;
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

			if (isJumping(individual)) {
				return;
			}

			state.velocity.x = state.velocity.x * 0.3f;
		}
	}
	
	
	/**
	 * True if a {@link Tile#isPassable()}, taking into account the path
	 */
	private boolean isTilePassable(
		final float x, 
		final float y, 
		final Topography topography, 
		final IndividualKineticsProcessingData kinematicsBean, 
		final ArtificialIntelligence ai
	) throws NoTileFoundException {
		final AITask current = ai.getCurrentTask();
		final Tile tile = topography.getTile(x, y, true);

		if (convertToWorldCoord(x, y, false).equals(kinematicsBean.jumpOff)) {
			return true;
		}
		
		// If we're on an empty tile it's obviously passable
		if (tile instanceof EmptyTile) {
			return true;
		}

		// If we're on a platform and we're GoingToLocation
		// then check to see if the tile is part of the path, if it is, then not passable, otherwise passable
		if (tile.isPlatformTile) {
			if (current instanceof GoToLocation) {
				return !((GoToLocation)current).isPartOfPath(new Vector2(x, y + TILE_SIZE));
			} else if (current instanceof CompositeAITask) {
				final AITask subTask = ((CompositeAITask) current).getCurrentTask();
				if (subTask instanceof GoToLocation) {
					return !((GoToLocation)subTask).isPartOfPath(new Vector2(x, y + TILE_SIZE));
				} else if (subTask instanceof GoToMovingLocation) {
					return !((GoToMovingLocation)subTask).getCurrentGoToLocation().isPartOfPath(new Vector2(x, y + TILE_SIZE));
				} else if (subTask instanceof JitGoToLocation) {
					AITask task = ((JitGoToLocation) subTask).getTask();
					if (task == null) {
						((JitGoToLocation) subTask).initialise();
					}
					task = ((JitGoToLocation) subTask).getTask();
					return !((GoToLocation) task).isPartOfPath(new Vector2(x, y + TILE_SIZE));
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

		//By this point we're not empty, and we're not a platform, not passable
		return false;
	}
}