package bloodandmithril.character.ai.task.speak;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.core.Copyright;

/**
 * Executor for {@link Speak}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class SpeakExecutor implements AITaskExecutor {

	@Override
	public void execute(AITask aiTask, float delta) {
		Speak task = (Speak) aiTask;
		
		if (!task.spoken) {
			task.getHost().speak(task.text, task.duration);
			task.spoken = true;
		}		
	}

	
	@Override
	public boolean isComplete(AITask aiTask) {
		return ((Speak) aiTask).spoken;
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}
}