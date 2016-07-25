package bloodandmithril.character.ai.task.plantseed;

import static bloodandmithril.world.Domain.getIndividual;
import static bloodandmithril.world.Domain.getWorld;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.plantseed.PlantSeed.Plant;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.prop.PropPlacementService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Executes {@link Plant}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class PlantExecutor implements AITaskExecutor {
	
	@Inject private PropPlacementService propPlacementService;

	@Override
	public void execute(AITask aiTask, float delta) {
		Plant task = (Plant) aiTask;
		
		task.planted = true;
		final Individual individual = getIndividual(task.getHostId().getId());
		final int takeItem = individual.takeItem(task.getParent().toPlant.getSeed());
		task.getParent().toPlant.setWorldId(individual.getWorldId());
		if (takeItem == 1) {
			boolean canPlace = false;
			try {
				canPlace = propPlacementService.canPlaceAtCurrentPosition(task.getParent().toPlant);
			} catch (NoTileFoundException e) {}
			
			if (canPlace) {
				getWorld(getIndividual(task.getHostId().getId()).getWorldId()).props().addProp(task.getParent().toPlant);
			} else {
				individual.giveItem(task.getParent().toPlant.getSeed());
			}
		}
		Wiring.injector().getInstance(UserInterface.class).refreshRefreshableWindows();		
	}

	
	@Override
	public boolean isComplete(AITask aiTask) {
		return ((Plant) aiTask).planted;
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}
}