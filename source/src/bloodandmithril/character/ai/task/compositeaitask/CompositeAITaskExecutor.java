package bloodandmithril.character.ai.task.compositeaitask;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;

/**
 * {@link AITaskExecutor} implementation for {@link CompositeAITask}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class CompositeAITaskExecutor implements AITaskExecutor {


	@Override
	public void execute(final AITask aiTask, final float delta) {
		final CompositeAITask task = (CompositeAITask) aiTask;

		if (task.getCurrentTask() == null) {
			return;
		}

		final AITask currentTask = task.getCurrentTask();
		final AITaskExecutor executor = Wiring.injector().getInstance(currentTask.getClass().getAnnotation(ExecutedBy.class).value());

		if (executor.isComplete(currentTask)) {
			executor.uponCompletion(currentTask);
			task.setCurrentTask(task.tasks.poll());
		}

		if (task.getCurrentTask() != null) {
			final AITask nextTask = task.getCurrentTask();

			Wiring.injector()
			.getInstance(nextTask.getClass().getAnnotation(ExecutedBy.class).value())
			.execute(nextTask, delta);
		}
	}


	@Override
	public boolean isComplete(final AITask aiTask) {
		final CompositeAITask task = (CompositeAITask) aiTask;
		return task.getCurrentTask() == null;
	}


	@Override
	public boolean uponCompletion(final AITask aiTask) {
		return false;
	}
}