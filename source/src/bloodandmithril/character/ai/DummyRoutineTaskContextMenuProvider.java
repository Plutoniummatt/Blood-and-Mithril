package bloodandmithril.character.ai;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.components.ContextMenu;

/**
 * This should never actually be called
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class DummyRoutineTaskContextMenuProvider implements RoutineTaskContextMenuProvider {
	@Override
	public ContextMenu getDailyRoutineContextMenu(final Individual host, final DailyRoutine routine) {
		throw new RuntimeException();
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		throw new RuntimeException();
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		throw new RuntimeException();
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		throw new RuntimeException();
	}
}