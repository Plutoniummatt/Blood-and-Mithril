package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.core.BloodAndMithrilClient.setCursorBoundTask;
import static bloodandmithril.world.Domain.getIndividual;
import static bloodandmithril.world.Domain.getWorld;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
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
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.Util;
import bloodandmithril.util.cursorboundtask.ChooseAreaCursorBoundTask;
import bloodandmithril.util.datastructure.Wrapper;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
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
public final class Harvest extends CompositeAITask implements RoutineTask {
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
	public final class HarvestItem extends AITask {
		private static final long serialVersionUID = 7585777004625914828L;
		private boolean taskDone = false;

		/**
		 * Constructor
		 */
		public HarvestItem(IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public final String getShortDescription() {
			return "Harveseting";
		}


		@Override
		public final boolean isComplete() {
			return taskDone;
		}


		@Override
		public final boolean uponCompletion() {
			return false;
		}


		@Override
		public final void execute(float delta) {

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


	public static final class HarvestSelectedHarvestablesTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = -501687183987861906L;
		private final int hostId;
		private final int harvestableId;
		private final int worldId;

		public HarvestSelectedHarvestablesTaskGenerator(int hostId, int harvestableId, int worldId) {
			this.hostId = hostId;
			this.harvestableId = harvestableId;
			this.worldId = worldId;
		}

		@Override
		public AITask apply(Object input) {
			if (valid()) {
				try {
					return new Harvest(getIndividual(hostId), (Harvestable) getWorld(worldId).props().getProp(harvestableId));
				} catch (NoTileFoundException e) {
					return null;
				}
			}
			return null;
		}

		@Override
		public String getDailyRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public String getEntityVisibleRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public String getIndividualConditionRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public String getStimulusDrivenRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public boolean valid() {
			if (getWorld(worldId).props().hasProp(harvestableId)) {
				return Harvestable.class.isAssignableFrom(getWorld(worldId).props().getProp(harvestableId).getClass());
			}

			return false;
		}

		private String getDescription() {
			if (valid()) {
				return "Harvest " + getWorld(worldId).props().getProp(harvestableId).getTitle();
			}
			return "";
		}
	}


	public static final class HarvestAreaTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = 7331795787474572204L;

		private float left, right, top, bottom;
		private int hostId;

		/**
		 * Constructor
		 */
		public HarvestAreaTaskGenerator(Vector2 start, Vector2 finish, int hostId) {
			this.hostId = hostId;
			this.left 	= min(start.x, finish.x);
			this.right 	= max(start.x, finish.x);
			this.top 	= max(start.y, finish.y);
			this.bottom	= min(start.y, finish.y);
		}

		@Override
		public final AITask apply(Object input) {
			final Individual individual = getIndividual(hostId);
			World world = getWorld(individual.getWorldId());
			List<Integer> propsWithinBounds = world.getPositionalIndexMap().getEntitiesWithinBounds(Prop.class, left, right, top, bottom);

			final Wrapper<Harvest> task = new Wrapper<Harvest>(null);

			propsWithinBounds
			.stream()
			.filter(id -> {
				return Harvestable.class.isAssignableFrom(world.props().getProp(id).getClass());
			})
			.map(id -> {
				return (Harvestable) world.props().getProp(id);
			})
			.forEach(harvestable -> {
				try {
					if (task.t == null) {
						task.t = new Harvest(individual, harvestable);
					} else {
						task.t.appendTask(new Harvest(individual, harvestable));
					}
				} catch (Exception e) {}
			});

			return task.t;
		}
		@Override
		public final String getDailyRoutineDetailedDescription() {
			return getDescription();
		}
		@Override
		public final String getEntityVisibleRoutineDetailedDescription() {
			return getDescription();
		}
		@Override
		public final String getIndividualConditionRoutineDetailedDescription() {
			return getDescription();
		}
		@Override
		public final String getStimulusDrivenRoutineDetailedDescription() {
			return getDescription();
		}
		private String getDescription() {
			return "Harvest from a defined area";
		}
		@Override
		public boolean valid() {
			return true;
		}
	}


	private final ContextMenu getChoices(final Routine routine, final Individual host) {
		return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true,
			new ContextMenu.MenuItem(
				"Harvest area",
				() -> {
					setCursorBoundTask(
						new ChooseAreaCursorBoundTask(
							args -> {
								routine.setAiTaskGenerator(new HarvestAreaTaskGenerator((Vector2) args[0], (Vector2) args[1], host.getId().getId()));
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
							@Override
							public void keyPressed(int keyCode) {
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
					setCursorBoundTask(
						new CursorBoundTask(
								args -> {
									ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
									if (Domain.getActiveWorld() != null) {
										for (int propKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Prop.class, getMouseWorldX(), getMouseWorldY())) {
											Prop prop = Domain.getActiveWorld().props().getProp(propKey);
											if (Harvestable.class.isAssignableFrom(prop.getClass()) && prop.isMouseOver()) {
												toChooseFrom.addMenuItem(
													new MenuItem(
														"Harvest " + prop.getTitle(),
														() -> {
															routine.setAiTaskGenerator(new HarvestSelectedHarvestablesTaskGenerator(host.getId().getId(), prop.id, prop.getWorldId()));
														},
														Color.ORANGE,
														Color.GREEN,
														Color.GRAY,
														null
													)
												);
											}
										}
									}

									UserInterface.contextMenus.clear();
									toChooseFrom.x = getMouseScreenX();
									toChooseFrom.y = getMouseScreenY();
									UserInterface.contextMenus.add(toChooseFrom);
								},
								true
							) {
							@Override
							public void renderUIGuide() {
							}
							@Override
							public String getShortDescription() {
								return "Harvest (Press enter to finalise)";
							}
							@Override
							public CursorBoundTask getImmediateTask() {
								return this;
							}
							@Override
							public boolean executionConditionMet() {
								if (Domain.getActiveWorld() != null) {
									for (int propKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Prop.class, getMouseWorldX(), getMouseWorldY())) {
										Prop prop = Domain.getActiveWorld().props().getProp(propKey);
										if (Harvestable.class.isAssignableFrom(prop.getClass()) && prop.isMouseOver()) {
											return true;
										}
									}
								}

								return false;
							}
							@Override
							public boolean canCancel() {
								return true;
							}
							@Override
							public void keyPressed(int keyCode) {
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
	public final ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine) {
		return getChoices(routine, host);
	}


	@Override
	public final ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine routine) {
		return getChoices(routine, host);
	}


	@Override
	public final ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine) {
		return getChoices(routine, host);
	}


	@Override
	public final ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine) {
		return getChoices(routine, host);
	}
}