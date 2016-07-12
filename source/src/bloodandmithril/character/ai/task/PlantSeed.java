package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.pathfinding.PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace;
import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static bloodandmithril.ui.UserInterface.refreshRefreshableWindows;
import static bloodandmithril.world.Domain.getIndividual;
import static bloodandmithril.world.Domain.getWorld;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.plant.SeedItem;
import bloodandmithril.prop.plant.seed.SeedProp;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.cursorboundtask.PlantSeedCursorBoundTask;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A {@link CompositeAITask} that instructs the host to go to a location and plant a {@link SeedProp}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Plant seed")
public class PlantSeed extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = -3292272671119971752L;

	private final SeedProp toPlant;

	@Inject
	PlantSeed() {
		super(null, "");
		this.toPlant = null;
	}

	/**
	 * Constructor
	 */
	public PlantSeed(final Individual host, final SeedProp toPlant) throws NoTileFoundException {
		super(
			host.getId(),
			"Planting seed",
			goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(getGroundAboveOrBelowClosestEmptyOrPlatformSpace(toPlant.position, 10, Domain.getWorld(host.getWorldId())), Topography.TILE_SIZE),
				false,
				50f,
				true
			)
		);

		this.toPlant = toPlant;

		appendTask(new Plant(hostId));
	}


	@Override
	public boolean isComplete() {
		if (getHost().has(toPlant.getSeed()) == 0) {
			return true;
		}
		return super.isComplete();
	}


	/**
	 * The task representing the actual planting of the seed
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2014")
	public class Plant extends AITask {
		private static final long serialVersionUID = -5888097320153824059L;

		boolean planted;

		public Plant(final IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getShortDescription() {
			return "Planting seed";
		}


		@Override
		public boolean isComplete() {
			return planted;
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		public void execute(final float delta) {
			planted = true;
			final Individual individual = getIndividual(hostId.getId());
			final int takeItem = individual.takeItem(toPlant.getSeed());
			toPlant.setWorldId(individual.getWorldId());
			if (takeItem == 1) {
				if (toPlant.canPlaceAtCurrentPosition()) {
					getWorld(getIndividual(hostId.getId()).getWorldId()).props().addProp(toPlant);
				} else {
					individual.giveItem(toPlant.getSeed());
				}
			}
			refreshRefreshableWindows();
		}
	}


	public static class PlantSeedTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = 8576792777955769423L;
		private List<Vector2> locations;
		private int hostId;
		private SeedItem item;
		public PlantSeedTaskGenerator(final List<Vector2> locations, final int hostId, final SeedItem item) {
			this.locations = locations;
			this.hostId = hostId;
			this.item = item;
		}
		@Override
		public AITask apply(final Object input) {
			try {
				PlantSeed plantSeed = null;
				for (final Vector2 location : locations) {
					final SeedProp propSeed = item.getPropSeed();
					propSeed.position = location.cpy();

					if (plantSeed == null) {
						plantSeed = new PlantSeed(Domain.getIndividual(hostId), propSeed);
					} else {
						plantSeed.appendTask(new PlantSeed(Domain.getIndividual(hostId), propSeed));
					}
				}

				return plantSeed;
			} catch (final Exception e) {
				return null;
			}
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
		private String getDescription() {
			if (locations.size() == 1) {
				final Vector2 location = locations.get(0);
				return "Plant " + item.getSingular(false) + " at " + String.format("%.1f", location.x) + ", " + String.format("%.1f", location.y);
			} else {
				return "Plant " + item.getSingular(false) + " at multiple locations";
			}
		}
		@Override
		public boolean valid() {
			if (Domain.getIndividual(hostId).has(item) == 0) {
				return false;
			}

			try {
				for (final Vector2 location : locations) {
					final SeedProp propSeed = item.getPropSeed();
					propSeed.position = location.cpy();
					new PlantSeed(Domain.getIndividual(hostId), propSeed);
				}
				return true;
			} catch (final Exception e) {
				return false;
			}
		}
		@Override
		public void render() {
			UserInterface.shapeRenderer.begin(ShapeType.Line);
			UserInterface.shapeRenderer.setColor(Color.GREEN);
			Gdx.gl20.glLineWidth(2f);
			final Individual attacker = Domain.getIndividual(hostId);
			UserInterface.shapeRenderer.rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);

			UserInterface.shapeRenderer.setColor(Color.RED);
			for (final Vector2 location : locations) {
				UserInterface.shapeRenderer.circle(worldToScreenX(location.x), worldToScreenY(location.y), 3f);
			}
			UserInterface.shapeRenderer.end();
		}
	}


	private ContextMenu chooseSeedMenu(final Individual host, final Routine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), false);

		final Set<Item> seeds = host.getItemsSatisfyingPredicate(new SerializableMappingFunction<Entry<Item,Integer>, Boolean>() {
			private static final long serialVersionUID = -6464048831666670157L;
			@Override
			public Boolean apply(final Entry<Item, Integer> input) {
				return SeedItem.class.isAssignableFrom(input.getKey().getClass());
			}
		}).keySet();


		for (final Item item : seeds) {
			menu.addMenuItem(
				new MenuItem(
					item.getSingular(true),
					() -> {},
					Color.ORANGE,
					Color.GREEN,
					Color.GRAY,
					() -> { return chooseLocationMenu((SeedItem) item, host, routine);}
				)
			);
		}

		if (seeds.isEmpty()) {
			menu.addMenuItem(
				new MenuItem(
					"No seeds in inventory",
					() -> {},
					Color.GRAY,
					Color.GRAY,
					Color.GRAY,
					null
				)
			);
		}

		return menu;
	}


	private ContextMenu chooseLocationMenu(final SeedItem seed, final Individual planter, final Routine routine) {
		return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true,
			new MenuItem(
				"Choose locations (Press enter to finalise)",
				() -> {
					final PlantSeedCursorBoundTask cursorBoundTask = new PlantSeedCursorBoundTask(seed, planter, routine) {
						@Override
						public CursorBoundTask getImmediateTask() {
							return this;
						}
					};
					cursorBoundTask.setTask(new JITTask() {
						@Override
						public void execute(final Object... args) {
							try {
								final Vector2 coords = Wiring.injector().getInstance(GameClientStateTracker.class).getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);
								cursorBoundTask.getPlantingLocations().add(new Vector2(getMouseWorldX(), coords.y));
							} catch (final NoTileFoundException e) {
								return;
							}
						}
					});
					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(cursorBoundTask);
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
		return chooseSeedMenu(host, routine);
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		return chooseSeedMenu(host, routine);
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		return chooseSeedMenu(host, routine);
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		return chooseSeedMenu(host, routine);
	}
}