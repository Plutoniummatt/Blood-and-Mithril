package bloodandmithril.character.individuals;

import static bloodandmithril.character.ai.perception.Visible.getVisible;
import static bloodandmithril.character.individuals.Individual.Action.JUMP_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.JUMP_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.RUN_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.RUN_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT_COMBAT_ONE_HANDED;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT_COMBATONE_HANDED;
import static bloodandmithril.character.individuals.Individual.Action.WALK_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.WALK_RIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.getKeyMappings;
import static bloodandmithril.util.ComparisonUtil.obj;

import java.util.List;
import java.util.Map;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

/**
 * An {@link Individual} that is grounded, moves on ground.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class GroundTravellingIndividual extends Individual {
	private static final long serialVersionUID = 7634760818045237827L;

	/**
	 * Constructor
	 */
	protected GroundTravellingIndividual(
			IndividualIdentifier id,
			IndividualState state,
			int factionId,
			float inventoryMassCapacity,
			int inventoryVolumeCapacity,
			int maxRings,
			int width,
			int height,
			int safetyHeight,
			Box interactionBox,
			int worldId,
			int maximumConcurrentMeleeAttackers) {
		super(id, state, factionId, inventoryMassCapacity, inventoryVolumeCapacity, maxRings, width, height, safetyHeight, interactionBox, worldId, maximumConcurrentMeleeAttackers);
	}


	/**
	 * @return the Current animated action this {@link GroundTravellingIndividual} is performing.
	 */
	protected void updateCurrentAction() {
		// If we're attacking, return
		if (attacking()) {
			return;
		}

		// If we're jumping, return
		if (obj(getCurrentAction()).oneOf(JUMP_LEFT, JUMP_RIGHT)) {
			return;
		}

		// If we're moving to the right
		if (isCommandActive(getKeyMappings().moveRight.keyCode)) {
			// If walking, and current action is not walking right, then set action to walking right
			if (isCommandActive(getKeyMappings().walk.keyCode) && !getCurrentAction().equals(WALK_RIGHT)) {
				setCurrentAction(WALK_RIGHT);
				setAnimationTimer(0f);
			} else if (!isCommandActive(getKeyMappings().walk.keyCode) && !getCurrentAction().equals(RUN_RIGHT)) {
				// Otherwise if running, and current action is not running right, then set action to running right
				setCurrentAction(RUN_RIGHT);
				setAnimationTimer(0f);
			}

		// Same for if we're moving left
		} else if (isCommandActive(getKeyMappings().moveLeft.keyCode)) {
			if (isCommandActive(getKeyMappings().walk.keyCode) && !getCurrentAction().equals(WALK_LEFT)) {
				setCurrentAction(WALK_LEFT);
				setAnimationTimer(0f);
			} else if (!isCommandActive(getKeyMappings().walk.keyCode) && !getCurrentAction().equals(RUN_LEFT)) {
				setCurrentAction(RUN_LEFT);
				setAnimationTimer(0f);
			}

		// Otherwise we're standing still, set current to standing left/right depending on which direction we were facing before.
		} else {
			if (obj(getCurrentAction()).oneOf(WALK_RIGHT, RUN_RIGHT, STAND_RIGHT, STAND_RIGHT_COMBATONE_HANDED)) {
				setCurrentAction(inCombatStance() ? STAND_RIGHT_COMBATONE_HANDED : STAND_RIGHT);
			} else {
				setCurrentAction(inCombatStance() ? STAND_LEFT_COMBAT_ONE_HANDED : STAND_LEFT);
			}
		}
	}


	@Override
	protected void respondToCommands() {
		//Horizontal movement
		Topography topography = Domain.getWorld(getWorldId()).getTopography();
		boolean attacking = attacking();

		try {
			if (Math.abs(getState().velocity.y) < 5f) {

				if (!attacking && isCommandActive(getKeyMappings().moveLeft.keyCode) && (Kinematics.canStepUp(-2, topography, getState(), getHeight(), getAI(), getKinematicsData()) || !Kinematics.obstructed(-2, topography, getState(), getHeight(), getAI(), getKinematicsData()))) {
					if (isCommandActive(getKeyMappings().walk.keyCode)) {
						getState().acceleration.x = getState().velocity.x > -getWalkSpeed() ? -400f : 400f;
					} else {
						getState().acceleration.x = getState().velocity.x > -getRunSpeed() ? -400f : 400f;
					}
				} else if (!attacking && isCommandActive(getKeyMappings().moveRight.keyCode) && (Kinematics.canStepUp(2, topography, getState(), getHeight(), getAI(), getKinematicsData()) || !Kinematics.obstructed(2, topography, getState(), getHeight(), getAI(), getKinematicsData()))) {
					if (isCommandActive(getKeyMappings().walk.keyCode)) {
						getState().acceleration.x = getState().velocity.x < getWalkSpeed() ? 400f : -400f;
					} else {
						getState().acceleration.x = getState().velocity.x < getRunSpeed() ? 400f : -400f;
					}
				} else {
					getState().acceleration.x = 0f;

					int offset = isCommandActive(getKeyMappings().moveRight.keyCode) ? 2 : isCommandActive(getKeyMappings().moveLeft.keyCode) ? -2 : 0;
					if (Kinematics.obstructed(offset, topography, getState(), getHeight(), getAI(), getKinematicsData()) && !Kinematics.canStepUp(offset, topography, getState(), getHeight(), getAI(), getKinematicsData()) && !(getAI().getCurrentTask() instanceof Idle)) {
						getAI().setCurrentTask(new Idle());
					}

					sendCommand(getKeyMappings().moveRight.keyCode, false);
					sendCommand(getKeyMappings().moveLeft.keyCode, false);
					sendCommand(getKeyMappings().walk.keyCode, false);
				}
			}
		} catch (NoTileFoundException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	protected void respondToAttackCommand() {
		switch (getCurrentAction()) {
			case ATTACK_LEFT_ONE_HANDED_WEAPON_STAB:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB:
			case ATTACK_LEFT_ONE_HANDED_WEAPON:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON:
			case ATTACK_LEFT_TWO_HANDED_WEAPON:
			case ATTACK_RIGHT_TWO_HANDED_WEAPON:
			case ATTACK_LEFT_ONE_HANDED_WEAPON_MINE:
			case ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE:
			case ATTACK_LEFT_SPEAR:
			case ATTACK_RIGHT_SPEAR:
			case ATTACK_LEFT_UNARMED:
			case ATTACK_RIGHT_UNARMED:
				if (getAnimationTimer() > getAnimationMap().get(getCurrentAction()).get(0).a.getAnimationDuration()) {
					setAnimationTimer(0f);
					if (getCurrentAction().left()) {
						setCurrentAction(STAND_LEFT_COMBAT_ONE_HANDED);
					} else {
						setCurrentAction(STAND_RIGHT_COMBATONE_HANDED);
					}
				}

			default:
				return;
		}
	}


	/**
	 * @return The Individual-specific animation map
	 */
	protected abstract Map<Action, List<WrapperForTwo<Animation, ShaderProgram>>> getAnimationMap();


	/**
	 * @return The current {@link Animation} based on the current {@link Action}
	 */
	@Override
	protected List<WrapperForTwo<Animation, ShaderProgram>> getCurrentAnimation() {
		return getAnimationMap().get(getCurrentAction());
	}


	@Override
	protected void internalUpdate(float delta) {
		respondToCommands();
		updateCurrentAction();
	}


	/**
	 * Performs a jump at the specified jump vector.
	 */
	public void jump(Vector2 jumpVector) {
		getState().velocity.x = jumpVector.x;
		getState().velocity.y = jumpVector.y;
		decreaseStamina(0.1f);

		SoundService.play(SoundService.femaleHit, getState().position.cpy(), true, getVisible(this));
		setCurrentAction(jumpVector.x < 0f ? Action.JUMP_LEFT : Action.JUMP_RIGHT);
	}
}