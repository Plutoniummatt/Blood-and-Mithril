package bloodandmithril.character.ai.task.takeitem;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.takeitem.TakeItem.Take;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.functions.IndividualSelected;
import bloodandmithril.networking.requests.RefreshWindowsResponse;
import bloodandmithril.networking.requests.SynchronizeIndividual;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Executes {@link Take}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class TakeExecutor implements AITaskExecutor {
	
	@Inject private UserInterface userInterface;

	@Override
	public void execute(AITask aiTask, float delta) {
		TakeItem task = ((Take) aiTask).getParent();
		
		final Individual individual = Domain.getIndividual(task.getHostId().getId());
		if (individual.getInteractionBox().overlapsWith(task.item.getPickupBox())) {
			if (!individual.canReceive(task.item)) {
				userInterface.addGlobalMessage("Inventory full", "Can not pick up item, inventory is full.", new IndividualSelected(individual.getId().getId()));
				task.inventoryFull = true;
				return;
			}

			individual.giveItem(task.item);
			if (task.item instanceof Equipable) {
				if (individual.getAvailableEquipmentSlots().get(((Equipable) task.item).slot).call()) {
					individual.equip((Equipable) task.item);
				}
			}
			Domain.getWorld(individual.getWorldId()).items().removeItem(task.item.getId());
		}		
	}
	

	@Override
	public boolean isComplete(AITask aiTask) {
		TakeItem task = ((Take) aiTask).getParent();
		return !Domain.getWorld(task.getHost().getWorldId()).items().hasItem(task.item.getId());
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		TakeItem task = ((Take) aiTask).getParent();
		
		if (ClientServerInterface.isClient()) {
			userInterface.refreshRefreshableWindows();
		} else {
			ClientServerInterface.sendNotification(-1, true, true,
				new SynchronizeIndividual.SynchronizeIndividualResponse(task.getHostId().getId(), System.currentTimeMillis()),
				new RefreshWindowsResponse()
			);
		}

		takeNextItem(task);
		return false || task.inventoryFull;
	}
	
	
	private void takeNextItem(TakeItem task) {
		
		if (!task.itemIds.isEmpty()) {
			final Integer next = task.itemIds.poll();
			final Individual individual = Domain.getIndividual(task.getHostId().getId());
			if (Domain.getWorld(individual.getWorldId()).items().getItem(next) != null) {
				try {
					task.appendTask(new TakeItem(
						individual,
						Domain.getWorld(individual.getWorldId()).items().getItem(next),
						task.itemIds
					));
				} catch (final NoTileFoundException e) {}
			} else {
				takeNextItem(task);
			}
		}
	}
}