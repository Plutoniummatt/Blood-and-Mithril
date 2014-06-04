package bloodandmithril.character.individuals;

import static bloodandmithril.character.individuals.Individual.Action.RUN_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.RUN_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.STAND_RIGHT;
import static bloodandmithril.character.individuals.Individual.Action.WALK_LEFT;
import static bloodandmithril.character.individuals.Individual.Action.WALK_RIGHT;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.util.ComparisonUtil.obj;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.Weapon;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Sets;

/**
 * An {@link Individual} that is grounded, moves on ground.
 *
 * @author Matt
 */
public abstract class GroundedIndividual extends Individual {
	private static final long serialVersionUID = 7634760818045237827L;

	/**
	 * Constructor
	 */
	protected GroundedIndividual(
			IndividualIdentifier id,
			IndividualState state,
			int factionId,
			float inventoryMassCapacity,
			int maxRings,
			int width,
			int height,
			int safetyHeight,
			Box interactionBox,
			int worldId,
			int maximumConcurrentMeleeAttackers) {
		super(id, state, factionId, inventoryMassCapacity, maxRings, width, height, safetyHeight, interactionBox, worldId, maximumConcurrentMeleeAttackers);
	}


	public abstract float getWalkSpeed();
	public abstract float getRunSpeed();


	/**
	 * @return the Current animated action this {@link GroundedIndividual} is performing.
	 */
	protected void updateCurrentAction() {
		// If we're moving to the right
		if (getState().velocity.x > 0) {
			// If walking, and current action is not walking right, then set action to walking right
			if (isCommandActive(KeyMappings.walk) && !currentAction.equals(WALK_RIGHT)) {
				currentAction = WALK_RIGHT;
				animationTimer = 0f;
			} else if (!isCommandActive(KeyMappings.walk) && !currentAction.equals(RUN_RIGHT)) {
				// Otherwise if running, and current action is not running right, then set action to running right
				currentAction = RUN_RIGHT;
				animationTimer = 0f;
			}

		// Same for if we're moving left
		} else if (getState().velocity.x < 0) {
			if (isCommandActive(KeyMappings.walk) && !currentAction.equals(WALK_LEFT)) {
				currentAction = WALK_LEFT;
				animationTimer = 0f;
			} else if (!isCommandActive(KeyMappings.walk) && !currentAction.equals(RUN_LEFT)) {
				currentAction = RUN_LEFT;
				animationTimer = 0f;
			}

		// Otherwise we're standing still, if current action is not standing, then set current to standing left/right depending on which direction we were facing before.
		} else if (getState().velocity.x == 0 && !currentAction.equals(STAND_LEFT) && !currentAction.equals(STAND_RIGHT)) {
			if (obj(currentAction).oneOf(WALK_RIGHT, RUN_RIGHT, STAND_RIGHT)) {
				currentAction = STAND_RIGHT;
			} else {
				currentAction = STAND_LEFT;
			}
		}
	}


	@Override
	protected void respondToCommands() {
		//Horizontal movement
		Topography topography = Domain.getWorld(getWorldId()).getTopography();
		if (Math.abs(getState().velocity.y) < 5f) {
			if (isCommandActive(KeyMappings.moveLeft) && (Kinematics.canStepUp(-2, topography, getState(), getHeight(), getAI(), getKinematicsData()) || !Kinematics.obstructed(-2, topography, getState(), getHeight(), getAI(), getKinematicsData()))) {
				if (isCommandActive(KeyMappings.walk)) {
					getState().velocity.x = -getWalkSpeed();
				} else {
					getState().velocity.x = -getRunSpeed();
				}
			} else if (isCommandActive(KeyMappings.moveRight) && (Kinematics.canStepUp(2, topography, getState(), getHeight(), getAI(), getKinematicsData()) || !Kinematics.obstructed(2, topography, getState(), getHeight(), getAI(), getKinematicsData()))) {
				if (isCommandActive(KeyMappings.walk)) {
					getState().velocity.x = getWalkSpeed();
				} else {
					getState().velocity.x = getRunSpeed();
				}
			} else {
				getState().velocity.x = 0f;
				getState().acceleration.x = 0f;

				int offset = isCommandActive(KeyMappings.moveRight) ? 2 : isCommandActive(KeyMappings.moveLeft) ? -2 : 0;
				if (Kinematics.obstructed(offset, topography, getState(), getHeight(), getAI(), getKinematicsData()) && !Kinematics.canStepUp(offset, topography, getState(), getHeight(), getAI(), getKinematicsData()) && !(ai.getCurrentTask() instanceof Idle)) {
					ai.setCurrentTask(new Idle());
				}

				sendCommand(KeyMappings.moveRight, false);
				sendCommand(KeyMappings.moveLeft, false);
				sendCommand(KeyMappings.walk, false);
			}
		}
	}


	@Override
	protected void respondToAttackCommand() {
		// Don't bother if we're not attacking
		if (!obj(currentAction).oneOf(
				Action.ATTACK_LEFT_ONE_HANDED_WEAPON,
				Action.ATTACK_RIGHT_ONE_HANDED_WEAPON,
				Action.ATTACK_LEFT_TWO_HANDED_WEAPON,
				Action.ATTACK_LEFT_TWO_HANDED_WEAPON,
				Action.ATTACK_LEFT_SPEAR,
				Action.ATTACK_RIGHT_SPEAR
			)) {
			return;
		}

		float attackDuration = getAttackDuration();
		if (animationTimer > attackDuration) {
			animationTimer = 0f;
			if (currentAction.flipXAnimation()) {
				currentAction = STAND_LEFT;
			} else {
				currentAction = STAND_RIGHT;
			}
		}
	}


	@SuppressWarnings("rawtypes")
	private float getAttackDuration() {
		Set<Item> keySet = Sets.newHashSet(getEquipped().keySet());
		for(Item item : keySet) {
			if (item instanceof Weapon) {
				return ((Weapon)item).getBaseAttackDuration();
			}
		}

		// Unarmed attack duration
		return 1f;
	}


	@Override
	protected void internalRender() {
		// Draw the body, position is centre bottom of the frame
		Collection<Animation> currentAnimations = getCurrentAnimation();
		if (currentAnimations == null) {
			return;
		}

		spriteBatch.begin();
		spriteBatch.setShader(Shaders.pass);
		Shaders.pass.setUniformMatrix("u_projTrans", BloodAndMithrilClient.cam.combined);
		for (Animation animation : currentAnimations) {
			TextureRegion keyFrame = animation.getKeyFrame(animationTimer, true);
			spriteBatch.draw(
				keyFrame.getTexture(),
				getState().position.x - keyFrame.getRegionWidth()/2,
				getState().position.y,
				keyFrame.getRegionWidth(),
				keyFrame.getRegionHeight(),
				keyFrame.getRegionX(),
				keyFrame.getRegionY(),
				keyFrame.getRegionWidth(),
				keyFrame.getRegionHeight(),
				currentAction.flipXAnimation(),
				false
			);
		}
		spriteBatch.end();

		specificInternalRender();
		spriteBatch.flush();
	}


	/**
	 * Grounded individual implementation specific render method
	 */
	protected abstract void specificInternalRender();


	/**
	 * @return The Individual-specific animation map
	 */
	protected abstract Map<Action, List<Animation>> getAnimationMap();


	/**
	 * @return The current {@link Animation} based on the current {@link Action}
	 */
	protected List<Animation> getCurrentAnimation() {
		return getAnimationMap().get(currentAction);
	}


	@Override
	protected void internalUpdate(float delta) {
		respondToCommands();
		updateCurrentAction();
	}
}