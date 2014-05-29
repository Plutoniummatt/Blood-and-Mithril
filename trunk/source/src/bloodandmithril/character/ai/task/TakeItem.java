package bloodandmithril.character.ai.task;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.requests.RefreshWindows;
import bloodandmithril.csi.requests.SynchronizeIndividual;
import bloodandmithril.item.items.Item;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;

/**
 * A {@link CompositeAITask} consisting of:
 *
 * {@link GoToLocation} of the item.
 * then picking up the item
 *
 * @author Matt
 */
public class TakeItem extends CompositeAITask {
	private static final long serialVersionUID = 1L;
	private Item item;
	private Deque<Integer> itemIds = new ArrayDeque<>();

	/**
	 * Constructor
	 */
	public TakeItem(Individual host, Item item) {
		super(
			host.getId(),
			"Taking item",
			new GoToLocation(
				host,
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
	public TakeItem(Individual host, Collection<Item> items) {
		super(
			host.getId(),
			"Taking items"
		);

		for (Item item : items) {
			itemIds.addLast(item.getId());
		}

		appendTask(new TakeItem(
			host,
			Domain.getItems().get(itemIds.poll()),
			itemIds
		));
	}


	/**
	 * Take multiple items
	 */
	public TakeItem(Individual host, Item item, Deque<Integer> itemIds) {
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
		public String getDescription() {
			return "Taking item";
		}


		@Override
		public boolean isComplete() {
			return !Domain.getItems().containsKey(item.getId());
		}


		@Override
		public void uponCompletion() {
			if (ClientServerInterface.isClient()) {
				UserInterface.refreshRefreshableWindows();
			} else {
				ClientServerInterface.sendNotification(-1, true, true,
					new SynchronizeIndividual.SynchronizeIndividualResponse(hostId.getId(), System.currentTimeMillis()),
					new RefreshWindows.RefreshWindowsResponse()
				);
			}

			takeNextItem();
		}


		private void takeNextItem() {
			if (!itemIds.isEmpty()) {
				Integer next = itemIds.poll();
				if (Domain.getItems().get(next) != null) {
					appendTask(new TakeItem(
						Domain.getIndividuals().get(hostId.getId()),
						Domain.getItems().get(next),
						itemIds
					));
				} else {
					takeNextItem();
				}
			}
		}


		@Override
		public void execute(float delta) {
			Individual individual = Domain.getIndividuals().get(hostId.getId());
			if (individual.getInteractionBox().isWithinBox(item.getPosition())) {
				individual.giveItem(item);
				Domain.getItems().remove(item.getId());
			}
		}
	}
}