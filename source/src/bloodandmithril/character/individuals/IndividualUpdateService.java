package bloodandmithril.character.individuals;

import static bloodandmithril.character.combat.CombatService.getAttackPeriod;
import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.networking.ClientServerInterface.isServer;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.round;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.character.conditions.Condition;
import bloodandmithril.character.conditions.Exhaustion;
import bloodandmithril.character.conditions.Hunger;
import bloodandmithril.character.conditions.Thirst;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.performance.PositionalIndexingService;
import bloodandmithril.util.ParameterizedTask;
import bloodandmithril.util.Task;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Service for updating individuals
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class IndividualUpdateService {

	@Inject	private PositionalIndexingService positionalIndexingService;
	@Inject	private IndividualKinematicsUpdater individualKinematicsUpdater;

	/**
	 * Updates the individual
	 */
	public void update(final Individual indi, final float delta) {
		final float aiTaskDelay = 0.01f;
		indi.setTravelIconTimer(indi.getTravelIconTimer() + delta * 10f);
		indi.setSpeakTimer(indi.getSpeakTimer() - delta <= 0f ? 0f : indi.getSpeakTimer() - delta);

		// If chunk has not yet been loaded, do not update
		try {
			Domain.getWorld(indi.getWorldId()).getTopography().getTile(indi.getState().position, true);
		} catch (final NoTileFoundException e) {
			return;
		}

		// Update interaction box location
		indi.getInteractionBox().position.x = indi.getState().position.x;
		indi.getInteractionBox().position.y = indi.getState().position.y + indi.getHeight() / 2;

		// Update hitbox location
		indi.getHitBox().position.x = indi.getState().position.x;
		indi.getHitBox().position.y = indi.getState().position.y + indi.getHeight() / 2;

		if (indi.getCombatTimer() <= 0f) {
			indi.setCombatStance(false);
		}

		if (indi.inCombatStance() && !(indi.getAI().getCurrentTask() instanceof Attack)) {
			indi.setCombatTimer(indi.getCombatTimer() - delta);
		}

		indi.setAiReactionTimer(indi.getAiReactionTimer() + delta);
		if (indi.getAiReactionTimer() >= aiTaskDelay && indi.isAlive()) {
			indi.getAI().update(aiTaskDelay);
			indi.setAiReactionTimer(0f);
		}

		indi.setAnimationTimer(indi.getAnimationTimer() + delta);
		indi.setAttackTimer(indi.getAttackTimer() + delta);

		if (indi.isAlive()) {
			updateVitals(indi, delta);
		}

		synchronized (indi.getBeingAttackedBy()) {
			Sets.newHashSet(indi.getBeingAttackedBy().keySet()).stream().forEach(i -> {
				final Individual individual = Domain.getIndividual(i);
				if (indi.getBeingAttackedBy().get(i) <= System.currentTimeMillis() - round(getAttackPeriod(individual) * 1000D) - 1000L) {
					indi.getBeingAttackedBy().remove(i);
				} else {
					final AITask currentTask = individual.getAI().getCurrentTask();
					if (currentTask instanceof Attack) {
						if (!((Attack) currentTask).getTargets().contains(indi.getId().getId())) {
							indi.getBeingAttackedBy().remove(i);
						}
					} else {
						indi.getBeingAttackedBy().remove(i);
					}
				}
			});
		}

		indi.internalUpdate(delta);

		executeActionFrames(indi);

		if (indi.isAlive()) {
			indi.respondToCommands();
			indi.respondToAttackCommand();
		}

		try {
			individualKinematicsUpdater.update(delta, Domain.getWorld(indi.getWorldId()), indi);
		} catch (final NoTileFoundException e) {

		}

		updateConditions(indi, delta);
		positionalIndexingService.indexInvidivual(indi);

		Sets.newHashSet(indi.getEquipped().keySet()).forEach(equipped -> {
			((Equipable) equipped).update(indi, delta);
		});
	}


	/**
	 * Updates the vitals of this {@link Individual}
	 * @param indi
	 */
	private void updateVitals(final Individual indi, final float delta) {
		indi.heal(delta * indi.getState().healthRegen);

		indi.decreaseHunger(indi.hungerDrain() * (delta* 60f));
		indi.decreaseThirst(indi.thirstDrain() * (delta* 60f));

		final Controls controls = Wiring.injector().getInstance(Controls.class);

		if (indi.isWalking()) {
			if (indi.isCommandActive(controls.moveLeft.keyCode) || indi.isCommandActive(controls.moveRight.keyCode)) {
				indi.increaseStamina(delta * indi.getState().staminaRegen / 2f);
			} else {
				indi.increaseStamina(delta * indi.getState().staminaRegen);
			}
		} else {
			if (indi.isCommandActive(controls.moveLeft.keyCode) || indi.isCommandActive(controls.moveRight.keyCode)) {
				indi.decreaseStamina(indi.staminaDrain() * (delta* 60f));
			} else {
				indi.increaseStamina(delta * indi.getState().staminaRegen);
			}
		}

		if (indi.getState().hunger < 0.75f) {
			indi.addCondition(new Hunger(indi.getId().getId()));
		}

		if (indi.getState().thirst < 0.75f) {
			indi.addCondition(new Thirst(indi.getId().getId()));
		}

		if (indi.getState().stamina < 0.75f) {
			indi.addCondition(new Exhaustion(indi.getId().getId()));
		}
	}


	/**
	 * Performs the {@link Task} associated with the current frame of the animation of the current {@link Action}
	 */
	private void executeActionFrames(final Individual indi) {
		ParameterizedTask<Individual> task = null;
		try {
			task = indi.getActionFrames()
				.get(indi.getCurrentAction())
				.get(indi.getCurrentAnimation().get(0).a.getAnimation(indi).getKeyFrameIndex(indi.getAnimationTimer()));
		} catch (final NullPointerException e) {
			// Do nothing
		}

		if (indi.getPreviousActionFrameAction() == indi.getCurrentAction() &&
			indi.getPreviousActionFrame() == indi.getCurrentAnimation().get(0).a.getAnimation(indi).getKeyFrameIndex(indi.getAnimationTimer())) {
			return;
		}

		if (task != null) {
			task.execute(indi);
		}

		indi.setPreviousActionFrame(indi.getCurrentAnimation().get(0).a.getAnimation(indi).getKeyFrameIndex(indi.getAnimationTimer()));
		indi.setPreviousActionFrameAction(indi.getCurrentAction());
	}


	/**
	 * Update how this {@link Individual} is affected by its {@link Condition}s
	 */
	private void updateConditions(final Individual indi, final float delta) {
		// Reset regeneration values
		if (isServer()) {
			indi.getState().reset();

			for (final Condition condition : newArrayList(indi.getState().currentConditions)) {
				if (condition.isExpired()) {
					condition.uponExpiry();
					indi.getState().currentConditions.remove(condition);
				} else {
					condition.affect(indi, delta);
				}
			}
		}

		if (isClient()) {
			for (final Condition condition : newArrayList(indi.getState().currentConditions)) {
				condition.clientSideEffects(indi, delta);
			}
		}
	}
}