package bloodandmithril.character.ai.task.compositeaitask;

import java.util.ArrayDeque;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;

/**
 * An {@link AITask} that is a composite of multiple {@link AITask}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ExecutedBy(CompositeAITaskExecutor.class)
public class CompositeAITask extends AITask {
	private static final long serialVersionUID = -3769737563309697525L;

	/** A list of tasks to be executed in order */
	protected final ArrayDeque<AITask> tasks = new ArrayDeque<>();

	/** Current task of this {@link CompositeAITask} */
	private AITask currentTask;

	/** Description of this {@link CompositeAITask} */
	private final String description;

	/**
	 * Constructor
	 */
	protected CompositeAITask(final IndividualIdentifier hostId, final String description, final AITask... tasks) {
		super(hostId);
		this.description = description;

		for (final AITask task : tasks) {
			this.tasks.addLast(task);
		}

		setCurrentTask(this.tasks.poll());
	}


	/** Adds a task to the end of the queue */
	public final void appendTask(final AITask taskToAdd) {
		tasks.addLast(taskToAdd);

		if (getCurrentTask() == null) {
			setCurrentTask(tasks.poll());
		}
	}


	/** Adds a task to the front of the queue */
	protected final void setNextTask(final AITask taskToAdd) {
		tasks.addFirst(taskToAdd);
	}


	@Override
	public String getShortDescription() {
		return description;
	}


	public AITask getCurrentTask() {
		return currentTask;
	}


	public void setCurrentTask(final AITask currentTask) {
		this.currentTask = currentTask;
	}
}