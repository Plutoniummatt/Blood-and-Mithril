package bloodandmithril.character.ai.task.lockunlockcontainer;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.lockunlockcontainer.LockUnlockContainer.LockUnlock;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.world.Domain;

/**
 * Executor for {@link LockUnlock}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class LockUnlockExecutor implements AITaskExecutor {

	@Override
	public void execute(AITask aiTask, float delta) {
		LockUnlock task = (LockUnlock) aiTask;
		LockUnlockContainer parent = task.getParent();
		
		final Individual individual = Domain.getIndividual(task.getHostId().getId());
		
		individual.getInventory().keySet().stream().forEach(item -> {
			if (parent.lock) {
				if (parent.container.lock(item)) {
					return;
				}
			} else {
				if (parent.container.unlock(item)) {
					return;
				}
			}
		});

		if (individual.getInventory().isEmpty()) {
			if (parent.lock) {
				if (parent.container.lock(null)) {
					return;
				}
			} else {
				if (parent.container.unlock(null)) {
					return;
				}
			}
		}

		task.complete = true;		
	}
	

	@Override
	public boolean isComplete(AITask aiTask) {
		return ((LockUnlock) aiTask).complete;
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}

}