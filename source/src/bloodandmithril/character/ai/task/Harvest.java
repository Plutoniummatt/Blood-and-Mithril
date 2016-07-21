package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static bloodandmithril.world.Domain.getIndividual;
import static bloodandmithril.world.Domain.getWorld;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.VisiblePropFuture;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
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
import bloodandmithril.util.cursorboundtask.ChooseMultipleEntityCursorBoundTask;
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

	@Inject private transient GameClientStateTracker gameClientStateTracker;
	@Inject private transient UserInterface userInterface;

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
	public Harvest(final Individual host, final Harvestable harvestable) throws NoTileFoundException {
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
		public HarvestItem(final IndividualIdentifier hostId) {
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
		protected void internalExecute(final float delta) {

			final Individual host = Domain.getIndividual(hostId.getId());

			if (host.getInteractionBox().overlapsWith(harvestable.getBoundingBox())) {
				if (Domain.getWorld(host.getWorldId()).props().hasProp(harvestable.id)) {
					if (((Harvestable)harvestable).destroyUponHarvest()) {
						Domain.getWorld(host.getWorldId()).props().removeProp(harvestable.id);
					}

					if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
						ClientServerInterface.SendNotification.notifyRemoveProp(harvestable.id, harvestable.getWorldId());
					}

					if (ClientServerInterface.isClient() && ClientServerInterface.isServer()) {
						final Collection<Item> harvested = ((Harvestable)harvestable).harvest(true);
						final Individual individual = Domain.getIndividual(hostId.getId());
						if (harvested != null && !harvested.isEmpty()) {
							for (final Item item : harvested) {
								if (individual.canReceive(item)) {
									individual.giveItem(item);
								} else {
									Domain.getWorld(individual.getWorldId()).items().addItem(item, harvestable.position.cpy().add(0, 10f), new Vector2(40f, 0).rotate(Util.getRandom().nextFloat() * 360f));
								}
							}
						}
						taskDone = true;
						final InventoryWindow existingInventoryWindow = (InventoryWindow) Iterables.find(userInterface.getLayeredComponents(), new Predicate<Component>() {
							@Override
							public boolean apply(final Component input) {
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
						final Collection<Item> harvested = ((Harvestable)harvestable).harvest(true);
						if (harvested != null && !harvested.isEmpty()) {
							for (final Item item : harvested) {
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
		private final List<Integer> harvestableIds;
		private final int worldId;

		public HarvestSelectedHarvestablesTaskGenerator(final int hostId, final List<Integer> harvestableIds, final int worldId) {
			this.hostId = hostId;
			this.harvestableIds = harvestableIds;
			this.worldId = worldId;
		}

		@Override
		public AITask apply(final Object input) {
			if (valid()) {
				try {
					final List<Harvestable> validEntities = Lists.newLinkedList();
					for (final int i : harvestableIds) {
						if (Domain.getWorld(worldId).props().hasProp(i)) {
							final Prop prop = Domain.getWorld(worldId).props().getProp(i);
							if (Harvestable.class.isAssignableFrom(prop.getClass())) {
								validEntities.add((Harvestable) prop);
							}
						}
					}

					if (validEntities.isEmpty()) {
						return null;
					} else if (validEntities.size() == 1) {
						return new Harvest(getIndividual(hostId), validEntities.get(0));
					} else {
						final Harvest harvest = new Harvest(getIndividual(hostId), validEntities.get(0));
						validEntities.remove(0);
						for (final Harvestable h : validEntities) {
							harvest.appendTask(new Harvest(getIndividual(hostId), h));
						}

						return harvest;
					}
				} catch (final NoTileFoundException e) {
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
			for (final int i : harvestableIds) {
				if (getWorld(worldId).props().hasProp(i)) {
					if (Harvestable.class.isAssignableFrom(getWorld(worldId).props().getProp(i).getClass())) {
						return true;
					}
				}
			}

			return false;
		}

		private String getDescription() {
			if (valid()) {
				return "Harvest selected entities";
			}
			return "";
		}

		@Override
		public void render() {
			final List<Prop> validEntities = Lists.newLinkedList();
			for (final int i : harvestableIds) {
				if (Domain.getWorld(worldId).props().hasProp(i)) {
					final Prop prop = Domain.getWorld(worldId).props().getProp(i);
					if (Harvestable.class.isAssignableFrom(prop.getClass())) {
						validEntities.add(prop);
					}
				}
			}
			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);

			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(Color.GREEN);
			final Individual harvestable = Domain.getIndividual(hostId);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(harvestable.getState().position.x) - harvestable.getWidth()/2,
				worldToScreenY(harvestable.getState().position.y),
				harvestable.getWidth(),
				harvestable.getHeight()
			);
			userInterface.getShapeRenderer().setColor(Color.RED);
			Gdx.gl20.glLineWidth(2f);
			for (final Prop p : validEntities) {
				userInterface.getShapeRenderer().rect(
					worldToScreenX(p.position.x) - p.width/2,
					worldToScreenY(p.position.y),
					p.width,
					p.height
				);

			}
			userInterface.getShapeRenderer().end();
		}
	}


	public static final class HarvestAreaTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = 7331795787474572204L;

		private float left, right, top, bottom;
		private int hostId;

		/**
		 * Constructor
		 */
		public HarvestAreaTaskGenerator(final Vector2 start, final Vector2 finish, final int hostId) {
			this.hostId = hostId;
			this.left 	= min(start.x, finish.x);
			this.right 	= max(start.x, finish.x);
			this.top 	= max(start.y, finish.y);
			this.bottom	= min(start.y, finish.y);
		}

		@Override
		public final AITask apply(final Object input) {
			final Individual individual = getIndividual(hostId);
			final World world = getWorld(individual.getWorldId());
			final List<Integer> propsWithinBounds = world.getPositionalIndexMap().getEntitiesWithinBounds(Prop.class, left, right, top, bottom);

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
				} catch (final Exception e) {}
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
		@Override
		public void render() {
			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(Color.GREEN);
			final Individual harvester = Domain.getIndividual(hostId);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(harvester.getState().position.x) - harvester.getWidth()/2,
				worldToScreenY(harvester.getState().position.y),
				harvester.getWidth(),
				harvester.getHeight()
			);

			userInterface.getShapeRenderer().setColor(Color.RED);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(left),
				worldToScreenY(bottom),
				right - left,
				top - bottom
			);

			userInterface.getShapeRenderer().end();
		}
	}


	private final ContextMenu getChoices(final Routine routine, final Individual host) {
		return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true,
			new ContextMenu.MenuItem(
				"Harvest area",
				() -> {
					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
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
							public void keyPressed(final int keyCode) {
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
					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
						new ChooseMultipleEntityCursorBoundTask<Prop, Integer>(true, Prop.class) {
							@Override
							public boolean canAdd(final Prop f) {
								return Harvestable.class.isAssignableFrom(f.getClass());
							}
							@Override
							public Integer transform(final Prop f) {
								return f.id;
							}
							@Override
							public void renderUIGuide(final Graphics graphics) {
								final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
								userInterface.getShapeRenderer().begin(ShapeType.Line);
								userInterface.getShapeRenderer().setColor(Color.RED);
								Gdx.gl20.glLineWidth(2f);
								for (final int i : entities) {
									final Prop p = gameClientStateTracker.getActiveWorld().props().getProp(i);
									final Vector2 position = p.position;

									userInterface.getShapeRenderer().rect(
										worldToScreenX(position.x) - p.width/2,
										worldToScreenY(position.y),
										p.width,
										p.height
									);

								}
								userInterface.getShapeRenderer().end();
							}
							@Override
							public boolean executionConditionMet() {
								final Collection<Prop> nearbyEntities = gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Prop.class, getMouseWorldX(), getMouseWorldY());
								for (final Prop p : nearbyEntities) {
									if (p.isMouseOver() && Harvestable.class.isAssignableFrom(p.getClass())) {
										return true;
									}
								}
								return false;
							}
							@Override
							public String getShortDescription() {
								return "Choose entities to harvest (Press enter to finalise)";
							}
							@Override
							public void keyPressed(final int keyCode) {
								if (keyCode == Keys.ENTER) {
									routine.setAiTaskGenerator(new HarvestSelectedHarvestablesTaskGenerator(host.getId().getId(), entities, gameClientStateTracker.getActiveWorld().getWorldId()));
									Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(null);
								}
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


	public static final class HarvestVisibleEntityTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = -2298234351386598398L;
		private final VisiblePropFuture propId;
		private final int hostId, worldId;

		public HarvestVisibleEntityTaskGenerator(final int hostId, final int worldId, final VisiblePropFuture propId) {
			this.hostId = hostId;
			this.worldId = worldId;
			this.propId = propId;
		}

		@Override
		public final AITask apply(final Object input) {
			final Prop prop = Domain.getWorld(worldId).props().getProp(propId.call());
			if (Harvestable.class.isAssignableFrom(prop.getClass())) {
				try {
					return new Harvest(Domain.getIndividual(hostId), (Harvestable) prop);
				} catch (final NoTileFoundException e) {
					return null;
				}
			}

			return null;
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
			return Domain.getIndividual(hostId).getId().getSimpleName() + " harvests any visible harvestable entity";
		}

		@Override
		public final boolean valid() {
			return true;
		}

		@Override
		public void render() {
			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(Color.GREEN);
			Gdx.gl20.glLineWidth(2f);
			final Individual attacker = Domain.getIndividual(hostId);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);

			userInterface.getShapeRenderer().end();
		}
	}


	@Override
	public final ContextMenu getDailyRoutineContextMenu(final Individual host, final DailyRoutine routine) {
		return getChoices(routine, host);
	}


	@Override
	public final ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		final ContextMenu choices = getChoices(routine, host);

		final EntityVisible identificationFunction = routine.getIdentificationFunction();
		if (Harvestable.class.isAssignableFrom(identificationFunction.getEntity().a)) {
			choices.addFirst(
				new MenuItem(
					"Visible harvestable entity",
					() -> {
						routine.setAiTaskGenerator(new HarvestVisibleEntityTaskGenerator(host.getId().getId(), host.getWorldId(), new EntityVisibleRoutine.VisiblePropFuture(routine)));
					},
					Color.MAGENTA,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);
		}

		return choices;
	}


	@Override
	public final ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		return getChoices(routine, host);
	}


	@Override
	public final ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		return getChoices(routine, host);
	}
}