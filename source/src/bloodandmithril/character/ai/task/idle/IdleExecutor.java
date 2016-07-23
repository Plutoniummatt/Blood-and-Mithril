package bloodandmithril.character.ai.task.idle;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.core.Copyright;

/**
 * Executes {@link Idle}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class IdleExecutor implements AITaskExecutor {

	
	@Override
	public void execute(AITask aiTask, float delta) {
		// TODO Auto-generated method stub
	}

	
	@Override
	public boolean isComplete(AITask aiTask) {
		return true;
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}
}