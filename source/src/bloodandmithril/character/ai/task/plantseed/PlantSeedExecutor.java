package bloodandmithril.character.ai.task.plantseed;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.core.Copyright;

/**
 * Executes {@link PlantSeed}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class PlantSeedExecutor extends CompositeAITaskExecutor {

	@Override
	public boolean isComplete(AITask aiTask) {
		PlantSeed task = (PlantSeed) aiTask;
		
		if (task.getHost().has(task.toPlant.getSeed()) == 0) {
			return true;
		}
		return super.isComplete(aiTask);
	}
}
