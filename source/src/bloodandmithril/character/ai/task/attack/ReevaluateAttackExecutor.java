package bloodandmithril.character.ai.task.attack;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.attack.Attack.ReevaluateAttack;
import bloodandmithril.core.Copyright;

/**
 * No-op implementation of {@link ReevaluateAttack}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ReevaluateAttackExecutor implements AITaskExecutor {

	@Override
	public void execute(final AITask aiTask, final float delta) {
		// Do nothing
	}


	@Override
	public boolean isComplete(final AITask aiTask) {
		return true;
	}


	@Override
	public boolean uponCompletion(final AITask aiTask) {
		final Attack parent = ((ReevaluateAttack) aiTask).getParent();
		parent.appendTask(new Attack(parent.getHostId(), parent.getTargets()));
		return false;
	}
}