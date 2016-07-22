package bloodandmithril.character.ai.task.craft;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.craft.Craft.Crafting;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.world.Domain;

/**
 * Executes {@link Crafting}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class CraftingExecutor implements AITaskExecutor {


	@Override
	public void execute(final AITask aiTask, final float delta) {
		final Crafting task = (Crafting) aiTask;

		final CraftingStation craftingStation = (CraftingStation) Domain.getWorld(task.getHost().getWorldId()).props().getProp(task.craftingStationId);
		final Individual individual = Domain.getIndividual(task.getHostId().getId());

		if (individual == null || craftingStation == null) {
			task.stop = true;
			return;
		}

		if (!craftingStation.craft(task.getParent().item, individual, delta)) {
			task.occupied = true;
		}

		if (craftingStation.isFinished()) {
			if (task.getParent().bulk) {
				craftingStation.takeItem(individual);
			}
			task.getParent().quantity--;
		}
	}


	@Override
	public boolean isComplete(final AITask aiTask) {
		final Crafting task = (Crafting) aiTask;
		return task.occupied || task.getParent().quantity == 0 || task.stop;
	}


	@Override
	public boolean uponCompletion(final AITask aiTask) {
		return false;
	}
}