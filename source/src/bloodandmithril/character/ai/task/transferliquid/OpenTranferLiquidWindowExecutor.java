package bloodandmithril.character.ai.task.transferliquid;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.transferliquid.TransferLiquidFromContainer.OpenTranferLiquidWindow;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.world.Domain;

/**
 * Executes {@link OpenTranferLiquidWindow}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class OpenTranferLiquidWindowExecutor implements AITaskExecutor {

	@Override
	public void execute(AITask aiTask, float delta) {
		OpenTranferLiquidWindow task = (OpenTranferLiquidWindow) aiTask;
		
		if (Domain.getIndividual(task.getHostId().getId()).getDistanceFrom(Domain.getWorld(task.getHost().getWorldId()).props().getProp(task.getParent().containerId).position) > 64f) {
			return;
		}

		if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
		} else if (ClientServerInterface.isClient()) {
		}

		task.opened = true;
		Domain.getIndividual(task.getHostId().getId()).clearCommands();
	}

	
	@Override
	public boolean isComplete(AITask aiTask) {
		return ((OpenTranferLiquidWindow) aiTask).opened;
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}
}