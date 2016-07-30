package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Action.JUMP_LEFT;
import static bloodandmithril.character.individuals.Action.JUMP_RIGHT;
import static bloodandmithril.character.individuals.Action.RUN_LEFT;
import static bloodandmithril.character.individuals.Action.RUN_RIGHT;
import static bloodandmithril.character.individuals.Action.STAND_LEFT;
import static bloodandmithril.character.individuals.Action.STAND_LEFT_COMBAT_ONE_HANDED;
import static bloodandmithril.character.individuals.Action.STAND_RIGHT;
import static bloodandmithril.character.individuals.Action.STAND_RIGHT_COMBAT_ONE_HANDED;
import static bloodandmithril.character.individuals.Action.WALK_LEFT;
import static bloodandmithril.character.individuals.Action.WALK_RIGHT;
import static bloodandmithril.util.ComparisonUtil.obj;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.util.AnimationHelper.AnimationSwitcher;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;

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
		final boolean attacking = attacking();
			if (Math.abs(getState().velocity.y) < 5f) {

				final float walkSpeed = getWalkSpeed();
				final float runSpeed = getRunSpeed();
				final int accel = 1000;

				if (!attacking && isCommandActive(controls.moveLeft.keyCode)) {
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
				} else if (!attacking && isCommandActive(controls.moveRight.keyCode)) {
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

					sendCommand(controls.moveRight.keyCode, false);
					sendCommand(controls.moveLeft.keyCode, false);
					sendCommand(controls.walk.keyCode, false);
				}
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
}