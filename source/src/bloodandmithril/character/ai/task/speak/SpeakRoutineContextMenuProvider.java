package bloodandmithril.character.ai.task.speak;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import com.badlogic.gdx.graphics.Color;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTaskContextMenuProvider;
import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.ai.task.speak.Speak.SpeakTaskGenerator;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.window.TextInputWindow;

/**
 * Provides context menus for {@link Speak}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class SpeakRoutineContextMenuProvider implements RoutineTaskContextMenuProvider {
	
	@Inject private UserInterface userInterface;

	@Override
	public ContextMenu getDailyRoutineContextMenu(final Individual host, final DailyRoutine routine) {
		return getContextMenu(host, routine);
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		return getContextMenu(host, routine);
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		return getContextMenu(host, routine);
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		return getContextMenu(host, routine);
	}
	
	
	private ContextMenu getContextMenu(final Individual host, final Routine routine) {
		return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true, new ContextMenu.MenuItem(
			"Set text",
			() -> {
				userInterface.addLayeredComponentUnique(
					new TextInputWindow("speakSetText", 500, 100, "Input text", 300, 250, args -> {
						final String text = (String) args[0];
						routine.setAiTaskGenerator(new SpeakTaskGenerator(host, Math.max(7500, text.length() * 10), text));
					}, "Set", true, "")
				);
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			null
		));
	}
}
