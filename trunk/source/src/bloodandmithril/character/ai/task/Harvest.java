package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.setCursorBoundTask;

import java.util.Collection;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.Util;
import bloodandmithril.util.cursorboundtask.ChooseAreaCursorBoundTask;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Harvest a {@link Harvestable}
 *
 * {@link GoToLocation} of the tile.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Harvest")
public class Harvest extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = -4098455998844182430L;

	/** Coordinate of the {@link Harvestable} to harvest */
	private final Prop harvestable;

	@Inject
	Harvest() {
		super(null, "");
		this.harvestable = null;
	}

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
			return taskDone;
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


	private ContextMenu getChoices(final Routine routine) {
		return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true,
			new ContextMenu.MenuItem(
				"Harvest area",
				() -> {
					setCursorBoundTask(
						new ChooseAreaCursorBoundTask(
							args -> {
								routine.setAiTaskGenerator(null);
							},
							true
						) {
							@Override
							public String getShortDescription() {
								return "Choose area";
							}
							@Override
							public boolean executionConditionMet() {
								return true;
							}
							@Override
							public boolean canCancel() {
								return true;
							}
							@Override
							public CursorBoundTask getImmediateTask() {
								return null;
							}
						}
					);
				},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			),
			new ContextMenu.MenuItem(
				"Harvest selected",
				() -> {
					BloodAndMithrilClient.setCursorBoundTask(
						new CursorBoundTask(
								args -> {},
								true
							) {
							@Override
							public void renderUIGuide() {
							}
							@Override
							public String getShortDescription() {
								return null;
							}
							@Override
							public CursorBoundTask getImmediateTask() {
								return null;
							}
							@Override
							public boolean executionConditionMet() {
								return false;
							}
							@Override
							public boolean canCancel() {
								return false;
							}
						}
					);
				},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);
	}


	@Override
	public ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine) {
		return getChoices(routine);
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine) {
		return null;
	}
}