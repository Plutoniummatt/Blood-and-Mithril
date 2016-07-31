package bloodandmithril.character.ai.task.attack;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.individuals.Action;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Executes an {@link Attack}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class AttackExecutor extends CompositeAITaskExecutor {

	@Override
	public void execute(final AITask aiTask, final float delta) {
		final Attack attack = (Attack) aiTask;

		if (attack.getHost().isWalking()) {
			attack.getHost().setWalking(false);
		}

		super.execute(aiTask, delta);
	}


	@Override
	public final boolean isComplete(final AITask aiTask) {
		final Attack attack = (Attack) aiTask;
		return attack.getAlive() == null;
	}


	@Override
	public final boolean uponCompletion(final AITask aiTask) {
		final Individual host = aiTask.getHost();
		if (host.inCombatStance()) {
			host.setCurrentAction(host.getCurrentAction().left() ? Action.STAND_LEFT_COMBAT_ONE_HANDED : Action.STAND_RIGHT_COMBAT_ONE_HANDED);
		} else {
			host.setCurrentAction(host.getCurrentAction().left() ? Action.STAND_LEFT : Action.STAND_RIGHT);
		}
		host.setAnimationTimer(0f);

		if (!host.isWalking()) {
			host.setWalking(true);
		}
		return false;
	}
}