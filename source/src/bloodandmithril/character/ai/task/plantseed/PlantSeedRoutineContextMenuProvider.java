package bloodandmithril.character.ai.task.plantseed;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;

import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTaskContextMenuProvider;
import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.plant.SeedItem;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.cursorboundtask.PlantSeedCursorBoundTask;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Provides context menus for {@link PlantSeed}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class PlantSeedRoutineContextMenuProvider implements RoutineTaskContextMenuProvider {
	
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
}