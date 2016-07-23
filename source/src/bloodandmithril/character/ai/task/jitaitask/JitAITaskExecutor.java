package bloodandmithril.character.ai.task.jitaitask;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;

/**
 * Executes {@link JitAITask}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class JitAITaskExecutor implements AITaskExecutor {
	
	@Override
	public void execute(AITask aiTask, float delta) {
		JitAITask task = (JitAITask) aiTask;
		
		task.initialise();
		Wiring.injector().getInstance(task.task.getClass().getAnnotation(ExecutedBy.class).value()).execute(task.task, delta);
	}

	
	@Override
	public boolean isComplete(AITask aiTask) {
		JitAITask task = (JitAITask) aiTask;
		
		task.initialise();
		return Wiring.injector().getInstance(task.task.getClass().getAnnotation(ExecutedBy.class).value()).isComplete(task.task);
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		JitAITask task = (JitAITask) aiTask;
		
		task.initialise();
		return Wiring.injector().getInstance(task.task.getClass().getAnnotation(ExecutedBy.class).value()).uponCompletion(task.task);
	}
}