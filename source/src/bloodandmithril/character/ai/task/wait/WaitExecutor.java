package bloodandmithril.character.ai.task.wait;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.core.Copyright;

/**
 * Executes {@link Wait}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class WaitExecutor implements AITaskExecutor {

	@Override
	public void execute(final AITask aiTask, final float delta) {
		final Wait task = (Wait) aiTask;

		if (task.time < 0f) {
			task.complete = true;
			return;
		}
		task.time = task.time - (System.currentTimeMillis() - task.systemTimeSinceLastUpdate)/1000f;
		task.systemTimeSinceLastUpdate = System.currentTimeMillis();
	}


	@Override
	public boolean isComplete(final AITask aiTask) {
		return ((Wait) aiTask).complete;
	}


	@Override
	public boolean uponCompletion(final AITask aiTask) {
		return false;
	}
}