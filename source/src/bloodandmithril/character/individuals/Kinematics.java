package bloodandmithril.character.individuals;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;
import static java.lang.Math.abs;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.badlogic.gdx.math.Vector2;



/**
 * Processes Kinematics for {@link Individual}s
 *
 * @author Matt
 */
public interface Kinematics {

	/**
	 * Handles standard kinematics
	 */
	static void kinetics(float delta, World world, Individual individual) {
		Topography topography = Domain.getWorld(individual.getWorldId()).getTopography();
		IndividualKineticsProcessingData kinematicsBean = individual.getKinematicsData();
		IndividualState state = individual.getState();
		ArtificialIntelligence ai = individual.getAI();

		jumpOffLogic(topography, kinematicsBean, state, ai);

		//Stepping up
		if (individual.getKinematicsData().steppingUp) {
			if (kinematicsBean.steps >= TILE_SIZE) {
				kinematicsBean.steppingUp = false;
				state.position.y += TILE_SIZE - kinematicsBean.steps;
			} else {
				state.position.y = state.position.y + 4f;
				kinematicsBean.steps += 4f;
			}
		}

		//Calculate position
		state.position.add(state.velocity.cpy().mul(delta));

		//Calculate velocity based on acceleration, including gravity
		if (abs((state.velocity.y - world.getGravity() * delta) * delta) < TILE_SIZE/2) {
			state.velocity.y = state.velocity.y - (kinematicsBean.steppingUp ? 0 : delta * world.getGravity());
		} else {
			state.velocity.y = state.velocity.y * 0.8f;
		}
		state.velocity.add(state.acceleration.cpy().mul(delta));

		//Ground detection
		//If the position is not on an empty tile and is not a platform tile run the ground detection routine
		//If the position is on a platform tile and if the tile below current position is not an empty tile, run ground detection routine
		//If position below is a platform tile and the next waypoint is directly below current position, skip ground detection
		if (groundDetectionCriteriaMet(topography, state, ai, kinematicsBean) && !kinematicsBean.steppingUp) {
			state.velocity.y = 0f;

			if (state.position.y >= 0f) {
				if ((int)state.position.y % TILE_SIZE == 0) {
					state.position.y = (int)state.position.y / TILE_SIZE * TILE_SIZE;
				} else {
					state.position.y = (int)state.position.y / TILE_SIZE * TILE_SIZE + TILE_SIZE;
				}
			} else {
				state.position.y = (int)state.position.y / TILE_SIZE * TILE_SIZE;
			}
		} else if (state.position.y == 0f && !(topography.getTile(state.position.x, state.position.y - 1, true) instanceof Tile.EmptyTile)) {
			state.velocity.y = 0f;
		} else {
			if ((individual.isCommandActive(KeyMappings.moveRight) && state.velocity.x < 0f) ||
				(individual.isCommandActive(KeyMappings.moveLeft) && state.velocity.x > 0f) ||
				(!individual.isCommandActive(KeyMappings.moveLeft) && !individual.isCommandActive(KeyMappings.moveRight))) {
				state.velocity.x = state.velocity.x * 0.3f;
			}
			state.acceleration.x = 0f;
		}

		//Wall check routine, only perform this if we're moving
		if (state.velocity.x != 0 && obstructed(0, topography, state, individual.getHeight(), ai, kinematicsBean)) {
			if (canStepUp(0, topography, state, individual.getHeight(), ai, kinematicsBean)) {
				if (!kinematicsBean.steppingUp) {
					kinematicsBean.steppingUp = true;
					kinematicsBean.steps = 0;
				}
			} else if (!kinematicsBean.steppingUp) {
				boolean check = false;
				while (obstructed(0, topography, state, individual.getHeight(), ai, kinematicsBean)) {
					if (state.velocity.x > 0) {
 						state.position.x = state.position.x - 1;
					} else {
						state.position.x = state.position.x + 1;
					}
					check = true;
				}
				if (check) {
					state.velocity.x = 0;
					ai.setCurrentTask(new Idle());
				}
			}
		}
	}


	/**
	 * Sets {@link #jumpOff} to null if we've passed it
	 */
	static void jumpOffLogic(Topography topography, IndividualKineticsProcessingData kinematicsBean, IndividualState state, ArtificialIntelligence ai) {
		AITask currentTask = ai.getCurrentTask();
		if (currentTask instanceof GoToLocation) {
			if (((GoToLocation) currentTask).isAboveNext(state.position)) {
				jumpOff(topography, state, kinematicsBean);
				kinematicsBean.jumpedOff = false;
			}
		}

		if (kinematicsBean.jumpOff != null) {
			if (kinematicsBean.jumpedOff && !convertToWorldCoord(state.position, false).equals(kinematicsBean.jumpOff)) {
				kinematicsBean.jumpedOff = false;
				kinematicsBean.jumpOff = null;
			} else if (Math.abs(convertToWorldCoord(state.position, false).cpy().sub(kinematicsBean.jumpOff).len()) > 2 * TILE_SIZE) {
				kinematicsBean.jumpedOff = true;
			}
		}
	}


	/**
	 * Whether we should be running ground detection
	 */
	static boolean groundDetectionCriteriaMet(Topography topography, IndividualState state, ArtificialIntelligence ai, IndividualKineticsProcessingData kinematicsBean) {
		Tile currentTile = topography.getTile(state.position.x, state.position.y, true);
		Tile tileBelow = topography.getTile(state.position.x, state.position.y - TILE_SIZE/2, true);
		return (!(currentTile instanceof Tile.EmptyTile) && !currentTile.isPlatformTile || currentTile.isPlatformTile && !(tileBelow instanceof EmptyTile)) &&
			    !isToBeIgnored(state.position, kinematicsBean);
	}


	/** Whether this {@link Individual} is obstructed by {@link Tile}s */
	static boolean obstructed(int offsetX, Topography topography, IndividualState state, int height, ArtificialIntelligence ai, IndividualKineticsProcessingData kinematicsBean) {
		int blockspan = height/TILE_SIZE + (height % TILE_SIZE == 0 ? 0 : 1);
		for (int block = 0; block != blockspan; block++) {
			if (!isPassable(state.position.x + offsetX, state.position.y + TILE_SIZE/2 + TILE_SIZE * block, topography, kinematicsBean, ai)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Determines during {@link #kinetics(float)} whether we can step up
	 */
	static boolean canStepUp(int offsetX, Topography topography, IndividualState state, int height, ArtificialIntelligence ai, IndividualKineticsProcessingData kinematicsBean) {
		int blockspan = height/TILE_SIZE + (height % TILE_SIZE == 0 ? 0 : 1);

		for (int block = 1; block != blockspan + 1; block++) {
			if (!isPassable(state.position.x + offsetX, state.position.y + TILE_SIZE*block + TILE_SIZE/2, topography, kinematicsBean, ai)) {
				return false;
			}
		}
		return !isPassable(state.position.x + offsetX, state.position.y + TILE_SIZE/2, topography, kinematicsBean, ai);
	}


	/**
	 * True if a {@link Tile#isPassable()}, taking into account the path
	 */
	static boolean isPassable(float x, float y, Topography topography, IndividualKineticsProcessingData kinematicsBean, ArtificialIntelligence ai) {
		AITask current = ai.getCurrentTask();
		Tile tile = topography.getTile(x, y, true);

		if (convertToWorldCoord(x, y, false).equals(kinematicsBean.jumpOff)) {
			return true;
		}

		//If we're on an empty tile it's obviously passable
		if (tile instanceof EmptyTile) {
			return true;
		}

		//If we're on a platform and we're GoingToLocation, then check to see if the tile above is part of the path, if it is, then not passable, otherwise passable
		if (tile.isPlatformTile) {
			if (current instanceof GoToLocation) {
				return !((GoToLocation)current).isPartOfPath(new Vector2(x, y + TILE_SIZE));
			} else {
				return true;
			}
		}

		//By this point we're not empty, and we're not a platform, not passable
		return false;
	}


	/**
	 * Jump off the tile this {@link Individual} is currently standing on, as long as its a platform
	 */
	public static void jumpOff(Topography topography, IndividualState state, IndividualKineticsProcessingData kinematicsBean) {
		if (topography.getTile(state.position.x, state.position.y - TILE_SIZE/2, true).isPlatformTile) {
			kinematicsBean.jumpOff = convertToWorldCoord(state.position.x, state.position.y - TILE_SIZE/2, false);
		}
	}


	/**
	 * @return true if {@link Tile} at location is to be ignored, according to {@link #jumpOff}
	 */
	static boolean isToBeIgnored(Vector2 location, IndividualKineticsProcessingData kinematicsBean) {
		if (kinematicsBean.jumpOff != null) {
			return convertToWorldCoord(location, false).equals(kinematicsBean.jumpOff) || convertToWorldCoord(location.x, location.y - 1, false).equals(kinematicsBean.jumpOff);
		}
		return false;
	}
}