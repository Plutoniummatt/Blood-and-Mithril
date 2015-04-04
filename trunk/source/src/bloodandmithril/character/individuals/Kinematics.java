package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Individual.Action.JUMP_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.JUMP_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.getKeyMappings;
import static bloodandmithril.util.ComparisonUtil.obj;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;
import static java.lang.Math.abs;
import bloodandmithril.character.ai.AIProcessor.JitGoToLocation;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.CompositeAITask;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.GoToMovingLocation;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;



/**
 * Processes Kinematics for {@link Individual}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface Kinematics {

	/**
	 * Handles standard kinematics
	 */
	static void kinetics(float delta, World world, Individual individual) throws NoTileFoundException {
		Topography topography = Domain.getWorld(individual.getWorldId()).getTopography();
		IndividualKineticsProcessingData kinematicsData = individual.getKinematicsData();
		IndividualState state = individual.getState();
		ArtificialIntelligence ai = individual.getAI();

		jumpOffLogic(topography, kinematicsData, state, ai);

		//Stepping up
		if (individual.getKinematicsData().steppingUp) {
			if (kinematicsData.steps >= TILE_SIZE) {
				kinematicsData.steppingUp = false;
				state.position.y += TILE_SIZE - kinematicsData.steps;
			} else {
				state.position.y = state.position.y + 4f;
				kinematicsData.steps += 4f;
			}
		}

		//Calculate position
		Vector2 distanceTravelled = state.velocity.cpy().scl(delta);
		state.position.add(distanceTravelled);
		if (distanceTravelled.y < 0f) {
			kinematicsData.distanceFallen -= distanceTravelled.y;
		}

		//Calculate velocity based on acceleration, including gravity
		if (abs((state.velocity.y - world.getGravity() * delta) * delta) < TILE_SIZE - 1) {
			state.velocity.y = state.velocity.y - (kinematicsData.steppingUp ? 0 : delta * world.getGravity());
		} else {
			state.velocity.y = state.velocity.y * 0.8f;
		}
		state.velocity.add(state.acceleration.cpy().scl(delta));

		//Ground detection
		//If the position is not on an empty tile and is not a platform tile run the ground detection routine
		//If the position is on a platform tile and if the tile below current position is not an empty tile, run ground detection routine
		//If position below is a platform tile and the next waypoint is directly below current position, skip ground detection
		friction(individual, state);
		boolean fallDamageApplies = kinematicsData.distanceFallen > 400f;
		if (groundDetectionCriteriaMet(topography, state, ai, kinematicsData) && !kinematicsData.steppingUp) {
			if (fallDamageApplies) {
				fallDamage(individual, kinematicsData);
			}
			state.velocity.y = 0f;
			kinematicsData.distanceFallen = 0f;
			
			if (obj(individual.getCurrentAction()).oneOf(JUMP_LEFT, JUMP_RIGHT)) {
				individual.setCurrentAction(state.velocity.x > 0 ? STAND_RIGHT : STAND_LEFT);
			}

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
			if (fallDamageApplies) {
				fallDamage(individual, kinematicsData);
			}
			state.velocity.y = 0f;
			kinematicsData.distanceFallen = 0f;
		} else {
			state.acceleration.x = 0f;
			state.velocity.x *= 0.995f;
		}

		if (abs(individual.getState().velocity.x) > individual.getRunSpeed() * 1.5f) {
			state.velocity.x = state.velocity.x * (obj(individual.getCurrentAction()).oneOf(JUMP_LEFT, JUMP_RIGHT) ? 0.99f : 0.65f);
		}

		//Wall check routine, only perform this if we're moving
		if (state.velocity.x != 0 && obstructed(0, topography, state, individual.getHeight(), ai, kinematicsData)) {
			if (canStepUp(0, topography, state, individual.getHeight(), ai, kinematicsData)) {
				if (!kinematicsData.steppingUp) {
					kinematicsData.steppingUp = true;
					if (ClientServerInterface.isServer()) {
						individual.decreaseStamina(0.02f);
					}
					kinematicsData.steps = 0;
				}
			} else if (!kinematicsData.steppingUp) {
				boolean check = false;
				while (obstructed(0, topography, state, individual.getHeight(), ai, kinematicsData)) {
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
					individual.sayStuck();
				}
			}
		}
	}


	static void fallDamage(Individual individual, IndividualKineticsProcessingData data) {
		individual.speak("Owwww!", 1000);
		
		float amount = (data.distanceFallen - 400f) / 20f;
		individual.damage(amount);
		individual.addFloatingText(String.format("%.2f", amount), Color.RED);
	}


	static void friction(Individual individual, IndividualState state) {
		if (individual.isCommandActive(getKeyMappings().moveRight.keyCode) && state.velocity.x < 0f ||
			individual.isCommandActive(getKeyMappings().moveLeft.keyCode) && state.velocity.x > 0f ||
			!individual.isCommandActive(getKeyMappings().moveLeft.keyCode) && !individual.isCommandActive(getKeyMappings().moveRight.keyCode)) {

			if (obj(individual.getCurrentAction()).oneOf(Action.JUMP_LEFT, Action.JUMP_RIGHT)) {
				return;
			}

			state.velocity.x = state.velocity.x * 0.4f;
		}
	}


	/**
	 * Sets {@link #jumpOff} to null if we've passed it
	 */
	static void jumpOffLogic(Topography topography, IndividualKineticsProcessingData kinematicsBean, IndividualState state, ArtificialIntelligence ai) throws NoTileFoundException {
		AITask currentTask = ai.getCurrentTask();
		if (currentTask instanceof GoToLocation) {
			if (((GoToLocation) currentTask).isAboveNext(state.position)) {
				jumpOff(topography, state, kinematicsBean);
				kinematicsBean.jumpedOff = false;
			}
		}

		if (currentTask instanceof CompositeAITask) {
			AITask subTask = ((CompositeAITask) currentTask).getCurrentTask();
			if (subTask instanceof GoToLocation) {
				if (((GoToLocation) subTask).isAboveNext(state.position)) {
					jumpOff(topography, state, kinematicsBean);
					kinematicsBean.jumpedOff = false;
				}
			} else if (subTask instanceof GoToMovingLocation) {
				if (((GoToMovingLocation) subTask).isAboveNext(state.position)) {
					jumpOff(topography, state, kinematicsBean);
					kinematicsBean.jumpedOff = false;
				}
			} else if (subTask instanceof JitGoToLocation) {
				AITask task = ((JitGoToLocation) subTask).getTask();
				if (task == null) {
					((JitGoToLocation) subTask).initialise();
				}
				task = ((JitGoToLocation) subTask).getTask();
				if (((GoToLocation) task).isAboveNext(state.position)) {
					jumpOff(topography, state, kinematicsBean);
					kinematicsBean.jumpedOff = false;
				}
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
	static boolean groundDetectionCriteriaMet(Topography topography, IndividualState state, ArtificialIntelligence ai, IndividualKineticsProcessingData kinematicsBean) throws NoTileFoundException {
		Tile currentTile = topography.getTile(state.position.x, state.position.y, true);
		Tile tileBelow = topography.getTile(state.position.x, state.position.y - TILE_SIZE/2, true);
		return (!(currentTile instanceof Tile.EmptyTile) && !currentTile.isPlatformTile || currentTile.isPlatformTile && !(tileBelow instanceof EmptyTile)) &&
			    !isToBeIgnored(state.position, kinematicsBean);
	}


	/** Whether this {@link Individual} is obstructed by {@link Tile}s */
	static boolean obstructed(int offsetX, Topography topography, IndividualState state, int height, ArtificialIntelligence ai, IndividualKineticsProcessingData kinematicsBean) throws NoTileFoundException {
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
	static boolean canStepUp(int offsetX, Topography topography, IndividualState state, int height, ArtificialIntelligence ai, IndividualKineticsProcessingData kinematicsBean) throws NoTileFoundException {
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
	static boolean isPassable(float x, float y, Topography topography, IndividualKineticsProcessingData kinematicsBean, ArtificialIntelligence ai) throws NoTileFoundException {
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
			} else if (current instanceof CompositeAITask) {
				AITask subTask = ((CompositeAITask) current).getCurrentTask();
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


	/**
	 * Jump off the tile this {@link Individual} is currently standing on, as long as its a platform
	 */
	public static void jumpOff(Topography topography, IndividualState state, IndividualKineticsProcessingData kinematicsBean) throws NoTileFoundException {
		if (topography.getTile(state.position.x, state.position.y - TILE_SIZE/2, true).isPlatformTile) {
			kinematicsBean.jumpOff = convertToWorldCoord(state.position.x, state.position.y - TILE_SIZE/2, false);
		}
	}


	/**
	 * @return true if {@link Tile} at location is to be ignored, according to {@link #jumpOff}
	 */
	static boolean isToBeIgnored(Vector2 location, IndividualKineticsProcessingData kinematicsBean) {
		if (kinematicsBean.jumpOff != null) {
			try {
				return convertToWorldCoord(location, false).equals(kinematicsBean.jumpOff) || convertToWorldCoord(location.x, location.y - 1, false).equals(kinematicsBean.jumpOff);
			} catch (NoTileFoundException e) {
				return false;
			}
		}
		return false;
	}
}