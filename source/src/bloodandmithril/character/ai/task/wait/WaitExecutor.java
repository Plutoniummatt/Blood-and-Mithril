package bloodandmithril.character.ai.task.wait;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.core.Copyright;
import bloodandmithril.world.Domain;

/**
 * Executes {@link Wait}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class WaitExecutor implements AITaskExecutor {

	@Override
	public void execute(AITask aiTask, float delta) {
		Wait task = (Wait) aiTask;
		
		Domain.getIndividual(task.getHostId().getId()).clearCommands();
		if (task.time < 0f) {
			task.complete = true;
			return;
		}
		task.time = task.time - (System.currentTimeMillis() - task.systemTimeSinceLastUpdate)/1000f;
		task.systemTimeSinceLastUpdate = System.currentTimeMillis();
	}

	
	@Override
	public boolean isComplete(AITask aiTask) {
		return ((Wait) aiTask).complete;
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}
}