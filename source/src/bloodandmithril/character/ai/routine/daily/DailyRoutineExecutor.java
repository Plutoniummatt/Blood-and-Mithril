package bloodandmithril.character.ai.routine.daily;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.world.Domain;

/**
 * Executes {@link DailyRoutine}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class DailyRoutineExecutor implements AITaskExecutor {


	@Override
	public void execute(final AITask aiTask, final float delta) {
		final DailyRoutine routine = (DailyRoutine) aiTask;

		final AITask task = routine.getTask();

		if (task != null) {
			Wiring.injector().getInstance(task.getClass().getAnnotation(ExecutedBy.class).value()).execute(task, delta);
		}
	}


	@Override
	public boolean isComplete(final AITask aiTask) {
		final DailyRoutine routine = (DailyRoutine) aiTask;

		if (routine.getTask() != null) {
			return Wiring.injector().getInstance(routine.getTask().getClass().getAnnotation(ExecutedBy.class).value()).isComplete(routine.getTask()) ||
				   !routine.areExecutionConditionsMet();
		}

		return false;
	}


	@Override
	public boolean uponCompletion(final AITask aiTask) {
		final DailyRoutine routine = (DailyRoutine) aiTask;

		if (routine.getTask() != null) {
			final AITask toNullify = routine.getTask();
			final AITaskExecutor executor = Wiring.injector().getInstance(toNullify.getClass().getAnnotation(ExecutedBy.class).value());

			if (executor.uponCompletion(toNullify)) {
				return true;
			} else {
				routine.setTask(null);
				routine.setLastExecutedEpoch(Domain.getWorld(routine.getHost().getWorldId()).getEpoch());
				return false;
			}
		}

		return false;
	}
}