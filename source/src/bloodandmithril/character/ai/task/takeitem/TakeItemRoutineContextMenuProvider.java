package bloodandmithril.character.ai.task.takeitem;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTaskContextMenuProvider;
import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine.VisibleItemFuture;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.ai.task.takeitem.TakeItem.LootAreaTaskGenerator;
import bloodandmithril.character.ai.task.takeitem.TakeItem.TakeVisibleItemTaskGenerator;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.items.Item;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.cursorboundtask.ChooseAreaCursorBoundTask;

/**
 * Provides context menus for {@link TakeItem}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class TakeItemRoutineContextMenuProvider implements RoutineTaskContextMenuProvider {

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
}