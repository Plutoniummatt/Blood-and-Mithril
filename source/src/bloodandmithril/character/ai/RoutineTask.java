package bloodandmithril.character.ai;

import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.components.ContextMenu;

/**
 * {@link RoutineTask}s are {@link AITask}s that are able to be used in {@link Routine}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface RoutineTask {

	/**
	 * @return Implementation specific context menu construction methods
	 */
	public ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine);
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine routine);
	public ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine);
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine);
}
