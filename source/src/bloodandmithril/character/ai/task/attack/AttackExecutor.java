package bloodandmithril.character.ai.task.attack;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.world.Domain;

/**
 * Executes an {@link Attack}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class AttackExecutor extends CompositeAITaskExecutor {

	@Inject private Controls controls;

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
		final Individual host = Domain.getIndividual(aiTask.getHostId().getId());
		host.sendCommand(controls.moveRight.keyCode, false);
		host.sendCommand(controls.moveLeft.keyCode, false);
		if (!aiTask.getHost().isWalking()) {
			aiTask.getHost().setWalking(true);
		}
		return false;
	}
}