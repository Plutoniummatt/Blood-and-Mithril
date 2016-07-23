package bloodandmithril.character.ai.task.trade;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.requests.TransferItems.TradeEntity;

/**
 * Executes {@link Trading}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class TradingExecutor implements AITaskExecutor {

	@Override
	public void execute(AITask aiTask, float delta) {
	}

	
	@Override
	public boolean isComplete(AITask aiTask) {
		Trading task = (Trading) aiTask;
		
		if (task.entity == TradeEntity.INDIVIDUAL) {
			if (!((Individual) task.proposee).isAlive()) {
				return true;
			}

			return task.proposer.getState().position.cpy().sub(((Individual) task.proposee).getState().position.cpy()).len() > 64;
		} else {
			return task.proposer.getState().position.cpy().sub(task.prop.position.cpy()).len() > 64;
		}
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}
}