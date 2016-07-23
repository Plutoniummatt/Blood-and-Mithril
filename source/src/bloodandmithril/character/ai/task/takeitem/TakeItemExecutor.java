package bloodandmithril.character.ai.task.takeitem;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.core.Copyright;

/**
 * Executor for {@link TakeItem}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class TakeItemExecutor extends CompositeAITaskExecutor {

	@Override
	public boolean isComplete(AITask aiTask) {
		return super.isComplete(aiTask) || ((TakeItem) aiTask).inventoryFull;
	}
}