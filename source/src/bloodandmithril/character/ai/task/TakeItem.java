package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.functions.IndividualSelected;
import bloodandmithril.networking.requests.RefreshWindows;
import bloodandmithril.networking.requests.SynchronizeIndividual;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.google.inject.Inject;

/**
 * A {@link CompositeAITask} consisting of:
 *
 * {@link GoToLocation} of the item.
 * then picking up the item
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Take item")
public class TakeItem extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = 1L;
	private Item item;
	private Deque<Integer> itemIds = new ArrayDeque<>();

	@Inject
	TakeItem() {
		super(null, "");
	}

	/**
	 * Constructor
	 */
	public TakeItem(Individual host, Item item) throws NoTileFoundException {
		super(
			host.getId(),
			"Taking item",
			goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(item.getPosition(), 10, Domain.getWorld(host.getWorldId())), Topography.TILE_SIZE),
				false,
				50f,
				true
			)
		);
		this.item = item;

		appendTask(new Take(hostId));
	}


	/**
	 * Take multiple items
	 */
	public TakeItem(Individual host, Collection<Item> items) throws NoTileFoundException {
		super(
			host.getId(),
			"Taking items"
		);

		for (Item item : items) {
			itemIds.addLast(item.getId());
		}

		appendTask(new TakeItem(
			host,
			Domain.getWorld(host.getWorldId()).items().getItem(itemIds.poll()),
			itemIds
		));
	}


	/**
	 * Take multiple items
	 */
	public TakeItem(Individual host, Item item, Deque<Integer> itemIds) throws NoTileFoundException {
		this(host, item);

		for (Integer id : itemIds) {
			this.itemIds.addLast(id);
		}
	}

	public class Take extends AITask {
		private static final long serialVersionUID = 8539704078732653173L;

		public Take(IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getShortDescription() {
			return "Taking item";
		}


		@Override
		public boolean isComplete() {
			return !Domain.getWorld(getHost().getWorldId()).items().hasItem(item.getId());
		}


		@Override
		public boolean uponCompletion() {
			if (ClientServerInterface.isClient()) {
				UserInterface.refreshRefreshableWindows();
			} else {
				ClientServerInterface.sendNotification(-1, true, true,
					new SynchronizeIndividual.SynchronizeIndividualResponse(hostId.getId(), System.currentTimeMillis()),
					new RefreshWindows.RefreshWindowsResponse()
				);
			}

			takeNextItem();
			return false;
		}


		private void takeNextItem() {
			if (!itemIds.isEmpty()) {
				Integer next = itemIds.poll();
				Individual individual = Domain.getIndividual(hostId.getId());
				if (Domain.getWorld(individual.getWorldId()).items().getItem(next) != null) {
					try {
						appendTask(new TakeItem(
							individual,
							Domain.getWorld(individual.getWorldId()).items().getItem(next),
							itemIds
						));
					} catch (NoTileFoundException e) {}
				} else {
					takeNextItem();
				}
			}
		}


		@Override
		public void execute(float delta) {
			Individual individual = Domain.getIndividual(hostId.getId());
			if (individual.getInteractionBox().overlapsWith(item.getPickupBox())) {
				if (!individual.canReceive(item)) {
					UserInterface.addGlobalMessage("Inventory full", "Can not pick up item, inventory is full.", new IndividualSelected(individual.getId().getId()));
					individual.getAI().setCurrentTask(new Idle());
					return;
				}

				individual.giveItem(item);
				if (item instanceof Equipable) {
					if (individual.getAvailableEquipmentSlots().get(((Equipable) item).slot).call()) {
						individual.equip((Equipable) item);
					}
				}
				Domain.getWorld(individual.getWorldId()).items().removeItem(item.getId());
			}
		}
	}


	@Override
	public ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu( Individual host, IndividualConditionRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine) {
		return null;
	}
}