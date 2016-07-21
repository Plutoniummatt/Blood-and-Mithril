package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static bloodandmithril.world.Domain.getIndividual;
import static bloodandmithril.world.Domain.getWorld;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
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
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.VisibleItemFuture;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.functions.IndividualSelected;
import bloodandmithril.networking.requests.RefreshWindowsResponse;
import bloodandmithril.networking.requests.SynchronizeIndividual;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.cursorboundtask.ChooseAreaCursorBoundTask;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

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

	@Inject
	private transient UserInterface userInterface;

	private static final long serialVersionUID = 1L;
	private Item item;
	private Deque<Integer> itemIds = new ArrayDeque<>();
	private boolean inventoryFull;

	@Inject
	TakeItem() {
		super(null, "");
	}

	/**
	 * Constructor
	 */
	public TakeItem(final Individual host, final Item item) throws NoTileFoundException {
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
	 * @see bloodandmithril.character.ai.AITask#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return super.isComplete() || inventoryFull;
	}


	/**
	 * Take multiple items
	 */
	public TakeItem(final Individual host, final Collection<Item> items) throws NoTileFoundException {
		super(
			host.getId(),
			"Taking items"
		);

		for (final Item item : items) {
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
	public TakeItem(final Individual host, final Item item, final Deque<Integer> itemIds) throws NoTileFoundException {
		this(host, item);

		for (final Integer id : itemIds) {
			this.itemIds.addLast(id);
		}
	}

	public class Take extends AITask {
		private static final long serialVersionUID = 8539704078732653173L;

		public Take(final IndividualIdentifier hostId) {
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
				userInterface.refreshRefreshableWindows();
			} else {
				ClientServerInterface.sendNotification(-1, true, true,
					new SynchronizeIndividual.SynchronizeIndividualResponse(hostId.getId(), System.currentTimeMillis()),
					new RefreshWindowsResponse()
				);
			}

			takeNextItem();
			return false || inventoryFull;
		}


		private void takeNextItem() {
			if (!itemIds.isEmpty()) {
				final Integer next = itemIds.poll();
				final Individual individual = Domain.getIndividual(hostId.getId());
				if (Domain.getWorld(individual.getWorldId()).items().getItem(next) != null) {
					try {
						appendTask(new TakeItem(
							individual,
							Domain.getWorld(individual.getWorldId()).items().getItem(next),
							itemIds
						));
					} catch (final NoTileFoundException e) {}
				} else {
					takeNextItem();
				}
			}
		}


		@Override
		protected void internalExecute(final float delta) {
			final Individual individual = Domain.getIndividual(hostId.getId());
			if (individual.getInteractionBox().overlapsWith(item.getPickupBox())) {
				if (!individual.canReceive(item)) {
					userInterface.addGlobalMessage("Inventory full", "Can not pick up item, inventory is full.", new IndividualSelected(individual.getId().getId()));
					inventoryFull = true;
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


	public static final class TakeVisibleItemTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = 2988235138275518617L;
		private final VisibleItemFuture itemId;
		private final int hostId, worldId;

		public TakeVisibleItemTaskGenerator(final int hostId, final int worldId, final VisibleItemFuture itemId) {
			this.hostId = hostId;
			this.worldId = worldId;
			this.itemId = itemId;
		}

		@Override
		public final AITask apply(final Object input) {
			final Item item = Domain.getWorld(worldId).items().getItem(itemId.call());
			try {
				return new TakeItem(Domain.getIndividual(hostId), item);
			} catch (final NoTileFoundException e) {
				return null;
			}
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
			return Domain.getIndividual(hostId).getId().getSimpleName() + " takes any visible item";
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


	public static class LootAreaTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = 115770354252263826L;

		private float left, right, top, bottom;
		private int hostId;

		/**
		 * Constructor
		 */
		public LootAreaTaskGenerator(final Vector2 start, final Vector2 finish, final int hostId) {
			this.hostId = hostId;
			this.left 	= min(start.x, finish.x);
			this.right 	= max(start.x, finish.x);
			this.top 	= max(start.y, finish.y);
			this.bottom	= min(start.y, finish.y);
		}

		@Override
		public AITask apply(final Object input) {
			final Individual individual = getIndividual(hostId);
			final World world = getWorld(individual.getWorldId());
			final List<Integer> itemsWithinBounds = world.getPositionalIndexMap().getEntitiesWithinBounds(Item.class, left, right, top, bottom);

			final List<Item> itemsToLoot = Lists.newLinkedList();

			itemsWithinBounds
			.stream()
			.map(id -> {
				return world.items().getItem(id);
			}).forEach(item -> {
				itemsToLoot.add(item);
			});

			try {
				return new TakeItem(individual, itemsToLoot);
			} catch (final NoTileFoundException e) {
				return null;
			}
		}

		@Override
		public String getDailyRoutineDetailedDescription() {
			return null;
		}

		@Override
		public String getEntityVisibleRoutineDetailedDescription() {
			return null;
		}

		@Override
		public String getIndividualConditionRoutineDetailedDescription() {
			return null;
		}

		@Override
		public String getStimulusDrivenRoutineDetailedDescription() {
			return null;
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
			final Individual looter = Domain.getIndividual(hostId);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(looter.getState().position.x) - looter.getWidth()/2,
				worldToScreenY(looter.getState().position.y),
				looter.getWidth(),
				looter.getHeight()
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


	private ContextMenu getContextMenu(final Routine routine, final Individual host) {
		return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true,
			new ContextMenu.MenuItem(
				"Choose area to loot",
				() -> {
					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
						new ChooseAreaCursorBoundTask(
							args -> {
								routine.setAiTaskGenerator(new LootAreaTaskGenerator((Vector2) args[0], (Vector2) args[1], host.getId().getId()));
							},
							true
						) {
							@Override
							public void keyPressed(final int keyCode) {
							}
							@Override
							public String getShortDescription() {
								return "Choose area to loot";
							}
							@Override
							public CursorBoundTask getImmediateTask() {
								return null;
							}
							@Override
							public boolean executionConditionMet() {
								return true;
							}
							@Override
							public boolean canCancel() {
								return true;
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
	public ContextMenu getDailyRoutineContextMenu(final Individual host, final DailyRoutine routine) {
		return getContextMenu(routine, host);
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		final ContextMenu contextMenu = getContextMenu(routine, host);

		final EntityVisible identificationFunction = routine.getIdentificationFunction();
		if (Item.class.isAssignableFrom(identificationFunction.getEntity().a)) {
			contextMenu.addFirst(
				new MenuItem(
					"Visible item",
					() -> {
						routine.setAiTaskGenerator(new TakeVisibleItemTaskGenerator(host.getId().getId(), host.getWorldId(), new VisibleItemFuture(routine)));
					},
					Color.MAGENTA,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);
		}

		return contextMenu;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		return getContextMenu(routine, host);
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		return getContextMenu(routine, host);
	}
}