package bloodandmithril.character.ai.task;

import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Item;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.topography.Topography;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Harvest a {@link Harvestable}
 *
 * {@link GoToLocation} of the tile.
 *
 * @author Matt
 */
public class Harvest extends CompositeAITask {
	private static final long serialVersionUID = -4098455998844182430L;

	/** Coordinate of the {@link Harvestable} to harvest */
	private final Harvestable harvestable;

	/**
	 * Constructor
	 *
	 * @param coordinate - World pixel coordinate of the {@link Harvestable} to harvest.
	 */
	public Harvest(Individual host, Harvestable harvestable) {
		super(
			host.getId(),
			"Mining",
			new GoToLocation(
				host,
				new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(harvestable.position, 10), 3 * Topography.TILE_SIZE),
				false,
				50f,
				true
			)
		);

		appendTask(this.new HarvestItem(host.getId()));

		this.harvestable = harvestable;
	}


	/**
	 * Task of mining a tile
	 *
	 * @author Matt
	 */
	public class HarvestItem extends AITask {
		private static final long serialVersionUID = 7585777004625914828L;

		/**
		 * Constructor
		 */
		public HarvestItem(IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getDescription() {
			return "Harveseting";
		}


		@Override
		public boolean isComplete() {
			return !GameWorld.props.containsKey(harvestable.id);
		}


		@Override
		public void uponCompletion() {
		}


		@Override
		public void execute() {

			Individual host = GameWorld.individuals.get(hostId.getId());

			if (host.getInteractionBox().isWithinBox(harvestable.position)) {
				if (GameWorld.props.containsKey(harvestable.id)) {
					if (harvestable.destroyUponHarvest()) {
						GameWorld.props.remove(harvestable.id);
					}

					if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
						ClientServerInterface.SendNotification.notifyRemoveProp(harvestable.id);
					}

					if (ClientServerInterface.isClient() && ClientServerInterface.isServer()) {
						Item harvested = harvestable.harvest();
						if (harvested != null) {
							GameWorld.individuals.get(hostId.getId()).giveItem(harvested);
						}
						InventoryWindow existingInventoryWindow = (InventoryWindow) Iterables.find(UserInterface.layeredComponents, new Predicate<Component>() {
							@Override
							public boolean apply(Component input) {
								if (input instanceof Window) {
									return ((Window) input).title.equals(hostId.getSimpleName() + " - Inventory");
								}
								return false;
							}
						}, null);

						if (existingInventoryWindow != null) {
							existingInventoryWindow.refresh();
						}
					} else if (ClientServerInterface.isServer()) {
						Item harvested = harvestable.harvest();
						if (harvested != null) {
							ClientServerInterface.SendNotification.notifyGiveItem(host.getId().getId(), harvested);
						}
					}
				}
			}
		}
	}
}