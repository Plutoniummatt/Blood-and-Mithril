package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Action.JUMP_LEFT;
import static bloodandmithril.character.individuals.Action.JUMP_RIGHT;
import static bloodandmithril.util.ComparisonUtil.obj;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.IndividualStateService;
import bloodandmithril.character.ai.AIProcessor.JitGoToLocation;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.gotolocation.GoToLocation;
import bloodandmithril.character.ai.task.gotolocation.GoToMovingLocation;
import bloodandmithril.character.ai.task.idle.Idle;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.ComparisonUtil;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.CornerType;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * Processes Kinematics for {@link Individual}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class IndividualKinematicsUpdater {

	@Inject private IndividualStateService individualStateService;

	/**
	 * @see bloodandmithril.character.individuals.IndividualKinematicsUpdater#update(float, bloodandmithril.world.World, bloodandmithril.character.individuals.Individual)
	 */
	public void update(final Individual individual, final float delta) throws NoTileFoundException {
		final World world = Domain.getWorld(individual.getWorldId());

		final Vector2 velocity = individual.getState().velocity;
		final Vector2 position = individual.getState().position;
		final Vector2 acceleration = individual.getState().acceleration;

		final Function<Vector2, Boolean> excludePassable = vector -> {
			try {
				return isTilePassable(vector.x, vector.y, world.getTopography(), individual.getKinematicsData(), individual.getAI());
			} catch (final NoTileFoundException e) {
				return false;
			}
		};

		final int surfaceReferenceOffset = 20;
		Tile surface = world.getTopography().getSurfaceTile(position.x, position.y + surfaceReferenceOffset, excludePassable);
		Vector2 surfaceLocation = world.getTopography().getLowestEmptyTileOrPlatformTileWorldCoordsExludeSpecified(position.x, position.y + surfaceReferenceOffset, true, excludePassable);
		final Vector2 surfaceVector = deriveSurfaceVector(position, surface.getCornerType());
		final boolean terrainDetected = terrainDetection(individual, velocity, position, surface, surfaceVector, surfaceLocation);
		updateTileDirectlyUnder(individual, world);

		surface = world.getTopography().getSurfaceTile(position.x, position.y + surfaceReferenceOffset, excludePassable);
		surfaceLocation = world.getTopography().getLowestEmptyTileOrPlatformTileWorldCoordsExludeSpecified(position.x, position.y + surfaceReferenceOffset, true, excludePassable);

		preventAccelerationIfMidAir(position, acceleration, surfaceLocation);
		updatePosition(delta, velocity, position, world.getTopography(), individual);
		updateTileDirectlyUnder(individual, world);
 		gravitation(delta, world, velocity, acceleration);

		if (!isJumping(individual) && terrainDetected) {
			friction(individual);
		}
	}


	/**
	 * Sets the {@link IndividualKineticsProcessingData#tileDirectlyBelowX} and {@link IndividualKineticsProcessingData#tileDirectlyBelowY}
	 */
	private void updateTileDirectlyUnder(final Individual individual, final World world) {
		try {
			final Vector2 surfaceLocation = world.getTopography().getLowestEmptyTileOrPlatformTileWorldCoordsOrHighestNonEmptyNonPlatform(
				individual.getState().position.x,
				individual.getState().position.y + 2f,
				true
			);

			individual.getKinematicsData().tileDirectlyBelowX = convertToWorldTileCoord(surfaceLocation.x);
			individual.getKinematicsData().tileDirectlyBelowY = convertToWorldTileCoord(surfaceLocation.y) - 1;

		} catch (final NoTileFoundException e) {
			individual.getKinematicsData().tileDirectlyBelowX = null;
			individual.getKinematicsData().tileDirectlyBelowY = null;
		}
	}


	/**
	 * Applies terrain detection and makes corrections
	 */
	private boolean terrainDetection(
		final Individual individual,
		final Vector2 velocity,
		final Vector2 position,
		final Tile surface,
		final Vector2 surfaceVector,
		final Vector2 surfaceLocation
	) {
		if (position.y < surfaceLocation.y) {
			final int cornerTileOffset = deriveCornerTileOffset(position, surface);
			float desiredYCoordinate = surfaceLocation.y - cornerTileOffset;
			
			if (isJumping(individual)) {
				if (terminateJump(individual, surfaceVector, velocity)) {
					position.y = desiredYCoordinate;
					velocity.y = 0f;
					individual.getKinematicsData().mostRecentTileX = Topography.convertToWorldTileCoord(surfaceLocation.x);
					individual.getKinematicsData().mostRecentTileY = Topography.convertToWorldTileCoord(surfaceLocation.y - TILE_SIZE / 2);
					return true;
				}
			} else {
				position.y = desiredYCoordinate;
				velocity.y = 0f;
				individual.getKinematicsData().mostRecentTileX = Topography.convertToWorldTileCoord(surfaceLocation.x);
				individual.getKinematicsData().mostRecentTileY = Topography.convertToWorldTileCoord(surfaceLocation.y - TILE_SIZE / 2);
				return true;
			}
		}

		return false;
	}


	/**
	 * @param individual
	 * @return true if the given individual is jumping
	 */
	private boolean isJumping(final Individual individual) {
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
		final Individual individual,
		final Vector2 surfaceVector,
		final Vector2 velocity
	) {
		if (velocity.dot(surfaceVector) <= 0) {
			individualStateService.stopMoving(individual);
			return true;
		}

		return false;
	}


	/**
	 * Derives the effective normal vector of the surface the individual is on
	 */
	private Vector2 deriveSurfaceVector(final Vector2 position, final CornerType cornerType) {
		Vector2 surfaceVector = new Vector2();

		switch (cornerType) {
		case SLOPE_UP:
			surfaceVector = new Vector2(-1f, 1f);
			break;
		case SLOPE_DOWN:
			surfaceVector = new Vector2(1f, 1f);
			break;
		case SLOPE_UP_THEN_DOWN:
			final int mod = (int) -position.x % TILE_SIZE;
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
	private int deriveCornerTileOffset(final Vector2 position, final Tile surface) {
		int triangleTileOffset = 0;
		final int x = round(position.x);

		switch (surface.getCornerType()) {
		case SLOPE_UP:
			if (position.x <= 0) {
				triangleTileOffset = - (int) position.x % TILE_SIZE;
			} else {
				triangleTileOffset = TILE_SIZE - (int) position.x % TILE_SIZE;
			}
			break;
		case SLOPE_DOWN:
			if (position.x <= 0) {
				triangleTileOffset = TILE_SIZE + (int) position.x % TILE_SIZE;
			} else {
				triangleTileOffset = (int) position.x % TILE_SIZE;
			}
			break;
		case SLOPE_UP_THEN_DOWN:
			if (x < -1) {
				final int mod = (int) -position.x % TILE_SIZE;
				if (mod <= TILE_SIZE/2) {
					triangleTileOffset = TILE_SIZE - 2 * mod;
				} else {
					triangleTileOffset = - TILE_SIZE + 2 * mod;
				}
			} else {
				final int mod = (int) position.x % TILE_SIZE;
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
	private void preventAccelerationIfMidAir(final Vector2 position, final Vector2 acceleration, final Vector2 surfaceLocation) {
		if (position.y >= surfaceLocation.y + TILE_SIZE/4) {
			acceleration.x = 0f;
		}
	}


	/**
	 * Updates the position based on velocity
	 */
	private void updatePosition(final float delta, final Vector2 velocity, final Vector2 position, final Topography topography, final Individual individual) {
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
			individualStateService.stopMoving(individual);

			position.x = position.x -= velocity.x * delta * 10;
			position.y = position.y -= velocity.y * delta * 10;
 			velocity.x = -previousVel.x * 0.3f;
			velocity.x = velocity.x < 0 ? min(-60f, velocity.x) : max(60f, velocity.x);
			velocity.y = 0;

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
	private boolean isInvalidPosition(final Individual individual, final Vector2 position, final Topography topography) {
		final int blockspan = individual.getHeight()/TILE_SIZE + (individual.getHeight() % TILE_SIZE == 0 ? 0 : 1);
		for (int block = isJumping(individual) ? 0 : 1; block != blockspan; block++) {
			try {
				final Tile tile = topography.getTile(position.x, position.y + block * TILE_SIZE + TILE_SIZE / 4, true);
				if (tile.getCornerType() == CornerType.NONE && !isTilePassable(
					position.x + block,
					position.y + block * TILE_SIZE + TILE_SIZE / 4,
					topography,
					individual.getKinematicsData(),
					individual.getAI()
				)) {
					return true;
				}
			} catch (final NoTileFoundException e) {
				return true;
			}
		}

		return false;
	}


	/**
	 * Applies gravitation to the individual
	 */
	private void gravitation(final float delta, final World world, final Vector2 velocity, final Vector2 acceleration) {
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
		final IndividualState state = individual.getState();

		final Action currentAction = individual.getCurrentAction();

		if (ComparisonUtil.obj(currentAction).oneOf(Action.WALK_RIGHT, Action.RUN_RIGHT) && state.velocity.x < 0f ||
				ComparisonUtil.obj(currentAction).oneOf(Action.WALK_LEFT, Action.RUN_LEFT) && state.velocity.x > 0f ||
			!ComparisonUtil.obj(currentAction).oneOf(Action.WALK_RIGHT, Action.RUN_RIGHT, Action.WALK_LEFT, Action.RUN_LEFT)) {

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

		// If we're on an empty tile it's obviously passable
		if (tile instanceof EmptyTile) {
			return true;
		}

		final int tileX = Topography.convertToWorldTileCoord(x);
		final int tileY = Topography.convertToWorldTileCoord(y);

		// If we're on the most recently stood on tile, then non-passable
		if (
			kinematicsBean.mostRecentTileX != null &&
			kinematicsBean.mostRecentTileY != null &&
			kinematicsBean.mostRecentTileX == tileX &&
			kinematicsBean.mostRecentTileY == tileY
		) {
			return false;
		}

		// Tile directly below is always not passable
		if (
			kinematicsBean.tileDirectlyBelowX != null &&
			kinematicsBean.tileDirectlyBelowY != null &&
			kinematicsBean.tileDirectlyBelowX == tileX &&
			kinematicsBean.tileDirectlyBelowY == tileY
		) {
			return false;
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
					return true;
				}
			} else {
				return true;
			}
		}

		//By this point we're not empty, and we're not a platform, not passable
		return false;
	}
}