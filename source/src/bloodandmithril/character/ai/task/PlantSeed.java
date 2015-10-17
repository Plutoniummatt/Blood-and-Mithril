package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.pathfinding.PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace;
import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.ui.UserInterface.refreshRefreshableWindows;
import static bloodandmithril.world.Domain.getIndividual;
import static bloodandmithril.world.Domain.getWorld;

import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
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
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.plant.SeedItem;
import bloodandmithril.prop.plant.seed.SeedProp;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
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
	public PlantSeed(Individual host, SeedProp toPlant) throws NoTileFoundException {
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


	/**
	 * The task representing the actual planting of the seed
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2014")
	public class Plant extends AITask {
		private static final long serialVersionUID = -5888097320153824059L;

		boolean planted;

		public Plant(IndividualIdentifier hostId) {
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
		public void execute(float delta) {
			planted = true;
			int takeItem = getIndividual(hostId.getId()).takeItem(toPlant.getSeed());
			if (takeItem == 1) {
				if (toPlant.canPlaceAtCurrentPosition()) {
					getWorld(getIndividual(hostId.getId()).getWorldId()).props().addProp(toPlant);
				}
			}
			refreshRefreshableWindows();
		}
	}


	public static class PlantSeedTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = 8576792777955769423L;
		private Vector2 location;
		private int hostId;
		private SeedItem item;
		public PlantSeedTaskGenerator(Vector2 location, int hostId, SeedItem item) {
			this.location = location;
			this.hostId = hostId;
			this.item = item;
		}
		@Override
		public AITask apply(Object input) {
			try {
				SeedProp propSeed = item.getPropSeed();
				propSeed.position = location.cpy();
				return new PlantSeed(Domain.getIndividual(hostId), propSeed);
			} catch (Exception e) {
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
			return "Plant " + item.getSingular(false) + " at " + String.format("%.1f", location.x) + ", " + String.format("%.1f", location.y);
		}
		@Override
		public boolean valid() {
			if (Domain.getIndividual(hostId).has(item) == 0) {
				return false;
			}

			try {
				SeedProp propSeed = item.getPropSeed();
				propSeed.position = location.cpy();
				new PlantSeed(Domain.getIndividual(hostId), propSeed);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}


	private ContextMenu chooseSeedMenu(Individual host, Routine routine) {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), false);

		Set<Item> seeds = host.getItemsSatisfyingPredicate(new SerializableMappingFunction<Entry<Item,Integer>, Boolean>() {
			private static final long serialVersionUID = -6464048831666670157L;
			@Override
			public Boolean apply(Entry<Item, Integer> input) {
				return SeedItem.class.isAssignableFrom(input.getKey().getClass());
			}
		}).keySet();


		for (Item item : seeds) {
			menu.addMenuItem(
				new MenuItem(
					item.getSingular(true),
					() -> {},
					Color.ORANGE,
					Color.GREEN,
					Color.GRAY,
					chooseLocationMenu((SeedItem) item, host, routine)
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


	private ContextMenu chooseLocationMenu(SeedItem seed, Individual planter, Routine routine) {
		return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true,
			new MenuItem(
				"Choose location",
				() -> {
					PlantSeedCursorBoundTask cursorBoundTask = new PlantSeedCursorBoundTask(seed, planter);
					cursorBoundTask.setTask(new JITTask() {
						@Override
						public void execute(Object... args) {
							Vector2 coords;
							try {
								coords = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);
							} catch (NoTileFoundException e) {
								return;
							}

							routine.setAiTaskGenerator(new PlantSeedTaskGenerator(new Vector2(getMouseWorldX(), coords.y), planter.getId().getId(), seed));
						}
					});
					BloodAndMithrilClient.setCursorBoundTask(cursorBoundTask);
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
		return chooseSeedMenu(host, routine);
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine routine) {
		return chooseSeedMenu(host, routine);
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine) {
		return chooseSeedMenu(host, routine);
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine) {
		return chooseSeedMenu(host, routine);
	}
}