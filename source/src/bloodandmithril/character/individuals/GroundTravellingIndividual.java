package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Individual.Action.JUMP_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.JUMP_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.RUN_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.RUN_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT_COMBAT_ONE_HANDED;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT_COMBAT_ONE_HANDED;
import static bloodandmithril.character.individuals.Individual.Action.WALK_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.WALK_RIGHT;
import static bloodandmithril.util.ComparisonUtil.obj;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.util.AnimationHelper.AnimationSwitcher;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

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
			final IndividualIdentifier id,
			final IndividualState state,
			final int factionId,
			final Behaviour naturalBehaviour,
			final float inventoryMassCapacity,
			final int inventoryVolumeCapacity,
			final int maxRings,
			final int width,
			final int height,
			final int safetyHeight,
			final Box interactionBox,
			final int worldId,
			final int maximumConcurrentMeleeAttackers) {
		super(id, state, factionId, naturalBehaviour, inventoryMassCapacity, inventoryVolumeCapacity, maxRings, width, height, safetyHeight, interactionBox, worldId, maximumConcurrentMeleeAttackers);
	}


	/**
	 * @return the Current animated action this {@link GroundTravellingIndividual} is performing.
	 */
	protected void updateCurrentAction() {
		final Controls controls = Wiring.injector().getInstance(Controls.class);
		// If dead, return
		if (!isAlive()) {
			return;
		}

		// If we're attacking, return
		if (attacking()) {
			return;
		}

		// If we're jumping, return
		if (obj(getCurrentAction()).oneOf(JUMP_LEFT, JUMP_RIGHT)) {
			return;
		}

		// If we're moving to the right
		if (isCommandActive(controls.moveRight.keyCode)) {
			// If walking, and current action is not walking right, then set action to walking right
			if (isCommandActive(controls.walk.keyCode) && !getCurrentAction().equals(WALK_RIGHT)) {
				setCurrentAction(WALK_RIGHT);
				setAnimationTimer(0f);
			} else if (!isCommandActive(controls.walk.keyCode) && !getCurrentAction().equals(RUN_RIGHT)) {
				// Otherwise if running, and current action is not running right, then set action to running right
				setCurrentAction(RUN_RIGHT);
				setAnimationTimer(0f);
			}

		// Same for if we're moving left
		} else if (isCommandActive(controls.moveLeft.keyCode)) {
			if (isCommandActive(controls.walk.keyCode) && !getCurrentAction().equals(WALK_LEFT)) {
				setCurrentAction(WALK_LEFT);
				setAnimationTimer(0f);
			} else if (!isCommandActive(controls.walk.keyCode) && !getCurrentAction().equals(RUN_LEFT)) {
				setCurrentAction(RUN_LEFT);
				setAnimationTimer(0f);
			}

		// Otherwise we're standing still, set current to standing left/right depending on which direction we were facing before.
		} else {
			if (obj(getCurrentAction()).oneOf(WALK_RIGHT, RUN_RIGHT, STAND_RIGHT, STAND_RIGHT_COMBAT_ONE_HANDED)) {
				setCurrentAction(inCombatStance() ? STAND_RIGHT_COMBAT_ONE_HANDED : STAND_RIGHT);
			} else {
				setCurrentAction(inCombatStance() ? STAND_LEFT_COMBAT_ONE_HANDED : STAND_LEFT);
			}
		}
	}


	@Override
	protected void respondToCommands() {
		if (!isAlive()) {
			return;
		}

		final Controls controls = Wiring.injector().getInstance(Controls.class);

		//Horizontal movement
		final Topography topography = Domain.getWorld(getWorldId()).getTopography();
		final boolean attacking = attacking();

		try {
			if (Math.abs(getState().velocity.y) < 5f) {

				final float walkSpeed = getWalkSpeed();
				final float runSpeed = getRunSpeed();
				final int accel = 2000;

				if (!attacking && isCommandActive(controls.moveLeft.keyCode) && (LegacyIndividualKinematicsUpdater.canStepUp(-2, topography, getState(), getHeight(), getAI(), getKinematicsData()) || !LegacyIndividualKinematicsUpdater.obstructed(-2, topography, getState(), getHeight(), getAI(), getKinematicsData()))) {
					if (isCommandActive(controls.walk.keyCode)) {
						if (getState().velocity.x > -walkSpeed) {
							getState().acceleration.x = -accel;
						} else {
							getState().acceleration.x = accel;
						}
					} else {
						if (getState().velocity.x > -runSpeed) {
							getState().acceleration.x = -accel;
						} else {
							getState().acceleration.x = accel;
						}
					}
				} else if (!attacking && isCommandActive(controls.moveRight.keyCode) && (LegacyIndividualKinematicsUpdater.canStepUp(2, topography, getState(), getHeight(), getAI(), getKinematicsData()) || !LegacyIndividualKinematicsUpdater.obstructed(2, topography, getState(), getHeight(), getAI(), getKinematicsData()))) {
					if (isCommandActive(controls.walk.keyCode)) {
						if (getState().velocity.x < walkSpeed) {
							getState().acceleration.x = accel;
						} else {
							getState().acceleration.x = -accel;
						}
					} else {
						if (getState().velocity.x < runSpeed) {
							getState().acceleration.x = accel;
						} else {
							getState().acceleration.x = -accel;
						}
					}
				} else {
					getState().acceleration.x = 0f;

					final int offset = isCommandActive(controls.moveRight.keyCode) ? 2 : isCommandActive(controls.moveLeft.keyCode) ? -2 : 0;
					if (LegacyIndividualKinematicsUpdater.obstructed(offset, topography, getState(), getHeight(), getAI(), getKinematicsData()) && !LegacyIndividualKinematicsUpdater.canStepUp(offset, topography, getState(), getHeight(), getAI(), getKinematicsData()) && !(getAI().getCurrentTask() instanceof Idle)) {
						getAI().setCurrentTask(new Idle());
					}

					sendCommand(controls.moveRight.keyCode, false);
					sendCommand(controls.moveLeft.keyCode, false);
					sendCommand(controls.walk.keyCode, false);
				}
			}
		} catch (final NoTileFoundException e) {
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
				if (getAnimationTimer() > getAnimationMap().get(getCurrentAction()).get(0).a.getAnimation(this).getAnimationDuration()) {
					setAnimationTimer(0f);
					if (getCurrentAction().left()) {
						setCurrentAction(STAND_LEFT_COMBAT_ONE_HANDED);
					} else {
						setCurrentAction(STAND_RIGHT_COMBAT_ONE_HANDED);
					}
				}

			default:
				return;
		}
	}


	/**
	 * @return The Individual-specific animation map
	 */
	protected abstract Map<Action, List<WrapperForTwo<AnimationSwitcher, ShaderProgram>>> getAnimationMap();


	/**
	 * @return The current {@link Animation} based on the current {@link Action}
	 */
	@Override
	public List<WrapperForTwo<AnimationSwitcher, ShaderProgram>> getCurrentAnimation() {
		return getAnimationMap().get(getCurrentAction());
	}


	@Override
	protected void internalUpdate(final float delta) {
		respondToCommands();
		updateCurrentAction();
	}


	/**
	 * Performs a jump at the specified jump vector.
	 */
	public void jump(final Vector2 jumpVector) {
		getState().velocity.x = jumpVector.x;
		getState().velocity.y = jumpVector.y;
		decreaseStamina(0.1f);

		setCurrentAction(jumpVector.x < 0f ? Action.JUMP_LEFT : Action.JUMP_RIGHT);
	}
}