package bloodandmithril.character.ai;

import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.components.ContextMenu;

/**
 * Provides {@link ContextMenu}s for {@link RoutineTask}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface RoutineTaskContextMenuProvider {

	/**
	 * @return Implementation specific context menu construction methods
	 */
	public ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine);
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine routine);
	public ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine);
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine);
}
