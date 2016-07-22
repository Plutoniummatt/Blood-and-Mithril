package bloodandmithril.character.ai.task.follow;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.world.Domain;

public class FollowExecutor extends CompositeAITaskExecutor {

	@Override
	public boolean isComplete(final AITask aiTask) {
		final Follow task = (Follow) aiTask;

		if (!task.followee.isAlive()) {
			return true;
		}

		if (task.terminationCondition != null) {
			return task.terminationCondition.call();
		} else {
			return super.isComplete(aiTask);
		}
	}


	@Override
	public boolean uponCompletion(final AITask aiTask) {
		final Follow task = (Follow) aiTask;

		if (task.terminationCondition != null && task.terminationCondition.call()) {
			return false;
		} else {
			final Individual follower = Domain.getIndividual(task.getHostId().getId());
			if (follower == null) {
				return false;
			}
			task.appendTask(new Follow(follower, task.followee, task.distance, task.terminationCondition));
			return true;
		}
	}
}