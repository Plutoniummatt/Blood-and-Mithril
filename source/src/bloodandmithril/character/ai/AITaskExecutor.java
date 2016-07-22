package bloodandmithril.character.ai;

import bloodandmithril.core.Copyright;

/**
 * Interface for {@link AITask} execution service
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface AITaskExecutor {

	/**
	 * Execute the {@link AITask}
	 */
	public void execute(AITask aiTask, float delta);


	/**
	 * @return whether or not this task has been completed
	 **/
	public boolean isComplete(final AITask aiTask);


	/**
	 * Called upon task completion
	 **/
	public boolean uponCompletion(final AITask aiTask);
}