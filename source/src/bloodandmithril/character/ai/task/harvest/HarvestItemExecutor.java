package bloodandmithril.character.ai.task.harvest;

import java.util.Collection;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.harvest.Harvest.HarvestItem;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

/**
 * Executes {@link HarvestItem}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class HarvestItemExecutor implements AITaskExecutor {
	
	@Inject private UserInterface userInterface;

	@Override
	public void execute(AITask aiTask, float delta) {
		HarvestItem task = (HarvestItem) aiTask;
		Harvest parent = task.getParent();
		
		final Individual host = Domain.getIndividual(task.getHostId().getId());

		if (host.getInteractionBox().overlapsWith(parent.harvestable.getBoundingBox())) {
			if (Domain.getWorld(host.getWorldId()).props().hasProp(parent.harvestable.id)) {
				if (((Harvestable) parent.harvestable).destroyUponHarvest()) {
					Domain.getWorld(host.getWorldId()).props().removeProp(parent.harvestable.id);
				}

				if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
					ClientServerInterface.SendNotification.notifyRemoveProp(parent.harvestable.id, parent.harvestable.getWorldId());
				}

				if (ClientServerInterface.isClient() && ClientServerInterface.isServer()) {
					final Collection<Item> harvested = ((Harvestable)parent.harvestable).harvest(true);
					final Individual individual = Domain.getIndividual(task.getHostId().getId());
					if (harvested != null && !harvested.isEmpty()) {
						for (final Item item : harvested) {
							if (individual.canReceive(item)) {
								individual.giveItem(item);
							} else {
								Domain.getWorld(individual.getWorldId()).items().addItem(item, parent.harvestable.position.cpy().add(0, 10f), new Vector2(40f, 0).rotate(Util.getRandom().nextFloat() * 360f));
							}
						}
					}
					task.taskDone = true;
					final InventoryWindow existingInventoryWindow = (InventoryWindow) Iterables.find(userInterface.getLayeredComponents(), new Predicate<Component>() {
						@Override
						public boolean apply(final Component input) {
							if (input instanceof Window) {
								return ((Window) input).title.equals(task.getHostId().getSimpleName() + " - Inventory");
							}
							return false;
						}
					}, null);

					if (existingInventoryWindow != null) {
						existingInventoryWindow.refresh();
					}
				} else if (ClientServerInterface.isServer()) {
					final Collection<Item> harvested = ((Harvestable) parent.harvestable).harvest(true);
					if (harvested != null && !harvested.isEmpty()) {
						for (final Item item : harvested) {
							ClientServerInterface.SendNotification.notifyGiveItem(host.getId().getId(), item, parent.harvestable.position.cpy().add(0, 10f));
						}
					}
				}
			}
		}		
	}

	
	@Override
	public boolean isComplete(AITask aiTask) {
		return ((HarvestItem) aiTask).taskDone;
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}
}
