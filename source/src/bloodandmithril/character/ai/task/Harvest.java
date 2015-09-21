package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;

import java.util.Collection;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Harvest a {@link Harvestable}
 *
 * {@link GoToLocation} of the tile.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Harvest extends CompositeAITask {
	private static final long serialVersionUID = -4098455998844182430L;

	/** Coordinate of the {@link Harvestable} to harvest */
	private final Prop harvestable;

	/**
	 * Constructor
	 *
	 * @param coordinate - World pixel coordinate of the {@link Harvestable} to harvest.
	 */
	public Harvest(Individual host, Harvestable harvestable) throws NoTileFoundException {
		super(
			host.getId(),
			"Harvesting",
			goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(((Prop) harvestable).position, 10, Domain.getWorld(host.getWorldId())), 3 * Topography.TILE_SIZE),
				false,
				50f,
				true
			)
		);

		appendTask(this.new HarvestItem(host.getId()));

		this.harvestable = (Prop) harvestable;
	}


	/**
	 * Task of mining a tile
	 *
	 * @author Matt
	 */
	public class HarvestItem extends AITask {
		private static final long serialVersionUID = 7585777004625914828L;
		private boolean taskDone = false;

		/**
		 * Constructor
		 */
		public HarvestItem(IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getShortDescription() {
			return "Harveseting";
		}


		@Override
		public boolean isComplete() {
			return taskDone ;
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		public void execute(float delta) {

			Individual host = Domain.getIndividual(hostId.getId());

			if (host.getInteractionBox().overlapsWith(harvestable.getBoundingBox())) {
				if (Domain.getWorld(host.getWorldId()).props().hasProp(harvestable.id)) {
					if (((Harvestable)harvestable).destroyUponHarvest()) {
						Domain.getWorld(host.getWorldId()).props().removeProp(harvestable.id);
					}

					if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
						ClientServerInterface.SendNotification.notifyRemoveProp(harvestable.id, harvestable.getWorldId());
					}

					if (ClientServerInterface.isClient() && ClientServerInterface.isServer()) {
						Collection<Item> harvested = ((Harvestable)harvestable).harvest(true);
						Individual individual = Domain.getIndividual(hostId.getId());
						if (harvested != null && !harvested.isEmpty()) {
							for (Item item : harvested) {
								if (individual.canReceive(item)) {
									individual.giveItem(item);
								} else {
									Domain.getWorld(individual.getWorldId()).items().addItem(item, harvestable.position.cpy().add(0, 10f), new Vector2(40f, 0).rotate(Util.getRandom().nextFloat() * 360f));
								}
							}
						}
						taskDone = true;
						InventoryWindow existingInventoryWindow = (InventoryWindow) Iterables.find(UserInterface.getLayeredComponents(), new Predicate<Component>() {
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
						Collection<Item> harvested = ((Harvestable)harvestable).harvest(true);
						if (harvested != null && !harvested.isEmpty()) {
							for (Item item : harvested) {
								ClientServerInterface.SendNotification.notifyGiveItem(host.getId().getId(), item, harvestable.position.cpy().add(0, 10f));
							}
						}
					}
				}
			}
		}
	}
}