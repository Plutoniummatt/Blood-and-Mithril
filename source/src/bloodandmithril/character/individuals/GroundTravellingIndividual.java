package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Action.STAND_LEFT_COMBAT_ONE_HANDED;
import static bloodandmithril.character.individuals.Action.STAND_RIGHT_COMBAT_ONE_HANDED;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import bloodandmithril.core.Copyright;
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
	}


	@Override
	protected void respondToCommands() {
		if (!isAlive()) {
			return;
		}

		//Horizontal movement
		final boolean attacking = attacking();
			if (Math.abs(getState().velocity.y) < 5f) {

				final float walkSpeed = getWalkSpeed();
				final float runSpeed = getRunSpeed();
				final int accel = 1000;

				final Action currentAction = getCurrentAction();

				if (!attacking && (currentAction == Action.WALK_LEFT || currentAction == Action.RUN_LEFT)) {
					if (currentAction == Action.WALK_LEFT) {
						if (!isWalking()) {
							setCurrentAction(Action.RUN_LEFT);
							setAnimationTimer(0f);
						}

						if (getState().velocity.x > -walkSpeed) {
							getState().acceleration.x = -accel;
						} else {
							getState().acceleration.x = accel;
						}
					} else {
						if (isWalking()) {
							setCurrentAction(Action.WALK_LEFT);
							setAnimationTimer(0f);
						}

						if (getState().velocity.x > -runSpeed) {
							getState().acceleration.x = -accel;
						} else {
							getState().acceleration.x = accel;
						}
					}
				} else if (!attacking && (currentAction == Action.WALK_RIGHT || currentAction == Action.RUN_RIGHT)) {
					if (currentAction == Action.WALK_RIGHT) {
						if (!isWalking()) {
							setCurrentAction(Action.RUN_RIGHT);
							setAnimationTimer(0f);
						}

						if (getState().velocity.x < walkSpeed) {
							getState().acceleration.x = accel;
						} else {
							getState().acceleration.x = -accel;
						}
					} else {
						if (isWalking()) {
							setCurrentAction(Action.WALK_RIGHT);
							setAnimationTimer(0f);
						}

						if (getState().velocity.x < runSpeed) {
							getState().acceleration.x = accel;
						} else {
							getState().acceleration.x = -accel;
						}
					}
				} else {
					getState().acceleration.x = 0f;
					setAnimationTimer(0f);
					if (inCombatStance()) {
						setCurrentAction(getCurrentAction().left() ? Action.STAND_LEFT_COMBAT_ONE_HANDED : Action.STAND_RIGHT_COMBAT_ONE_HANDED);
					} else {
						setCurrentAction(getCurrentAction().left() ? Action.STAND_LEFT : Action.STAND_RIGHT);
					}
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
						setAnimationTimer(0f);
					} else {
						setCurrentAction(STAND_RIGHT_COMBAT_ONE_HANDED);
						setAnimationTimer(0f);
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