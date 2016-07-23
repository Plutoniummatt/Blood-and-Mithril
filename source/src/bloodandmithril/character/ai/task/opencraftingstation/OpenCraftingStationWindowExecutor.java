package bloodandmithril.character.ai.task.opencraftingstation;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.opencraftingstation.OpenCraftingStation.OpenCraftingStationWindow;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.Domain;

@Singleton
@Copyright("Matthew Peck 2016")
public class OpenCraftingStationWindowExecutor implements AITaskExecutor {
	
	@Inject private UserInterface userInterface;
	
	@Override
	public void execute(AITask aiTask, float delta) {
		OpenCraftingStationWindow task = (OpenCraftingStationWindow) aiTask;
		
		if (Domain.getIndividual(task.getHostId().getId()).getDistanceFrom(Domain.getWorld(task.getHost().getWorldId()).props().getProp(task.craftingStationId).position) > 64f) {
			return;
		}

		if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
			ClientServerInterface.SendNotification.notifyOpenCraftingStationWindow(task.getHostId().getId(), task.craftingStationId, task.connectionId, task.getHost().getWorldId());
		} else if (ClientServerInterface.isClient()) {
			openCraftingStationWindow(Domain.getIndividual(task.getHostId().getId()), (CraftingStation) Domain.getWorld(task.getHost().getWorldId()).props().getProp(task.craftingStationId));
		}

		task.opened = true;
		Domain.getIndividual(task.getHostId().getId()).clearCommands();		
	}

	
	@Override
	public boolean isComplete(AITask aiTask) {
		return ((OpenCraftingStationWindow) aiTask).opened;
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}
	
	
	public void openCraftingStationWindow(final Individual individual, final CraftingStation craftingStation) {
		userInterface.addLayeredComponentUnique(craftingStation.getCraftingStationWindow(individual));
	}
}