package bloodandmithril.character.ai.routine.entityvisible;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;

/**
 * Executes {@link EntityVisibleRoutine}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class EntityVisibleExecutor implements AITaskExecutor {


	@Override
	public void execute(final AITask aiTask, final float delta) {
		final EntityVisibleRoutine routine = (EntityVisibleRoutine) aiTask;

		final AITask task = routine.getTask();

		if (task != null) {
			Wiring.injector().getInstance(task.getClass().getAnnotation(ExecutedBy.class).value()).execute(task, delta);
		}
	}


	@Override
	public boolean isComplete(final AITask aiTask) {
		final EntityVisibleRoutine routine = (EntityVisibleRoutine) aiTask;

		final AITask task = routine.getTask();
		if (task != null) {
			return Wiring.injector().getInstance(task.getClass().getAnnotation(ExecutedBy.class).value()).isComplete(task);
		}

		return false;
	}


	@Override
	public boolean uponCompletion(final AITask aiTask) {
		final EntityVisibleRoutine routine = (EntityVisibleRoutine) aiTask;

		if (routine.getTask() != null) {
			final AITask toNullify = routine.getTask();
			if (Wiring.injector().getInstance(toNullify.getClass().getAnnotation(ExecutedBy.class).value()).uponCompletion(toNullify)) {
				return true;
			} else {
				routine.setTask(null);
				return false;
			}
		}

		return false;
	}
}