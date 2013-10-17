package spritestar.character.ai.task;

import java.util.ArrayDeque;

import spritestar.character.Individual.IndividualIdentifier;
import spritestar.character.ai.AITask;

/**
 * An {@link AITask} that is a composite of multiple {@link AITask}s
 *
 * @author Matt
 */
public class CompositeAITask extends AITask {
	private static final long serialVersionUID = -3769737563309697525L;

	/** A list of tasks to be executed in order */
	private final ArrayDeque<AITask> tasks = new ArrayDeque<>();

	/** Current task of this {@link CompositeAITask} */
	protected AITask currentTask;

	/** Description of this {@link CompositeAITask} */
	private final String description;

	/**
	 * Constructor
	 */
	protected CompositeAITask(IndividualIdentifier hostId, String description, AITask... tasks) {
		super(hostId);
		this.description = description;

		for (AITask task : tasks) {
			this.tasks.addLast(task);
		}

		currentTask = this.tasks.poll();
	}


	/** Adds a task to the end of the queue */
	protected void appendTask(AITask taskToAdd) {
		tasks.addLast(taskToAdd);
	}


	/** Adds a task to the front of the queue */
	protected void setNextTask(AITask taskToAdd) {
		tasks.addFirst(taskToAdd);
	}


	/**
	 * @see spritestar.character.ai.AITask#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return currentTask == null;
	}


	/**
	 * @see spritestar.character.ai.AITask#execute()
	 */
	@Override
	public void execute() {
		if (currentTask.isComplete()) {
			currentTask.uponCompletion();
			currentTask = tasks.poll();
		}

		if (currentTask != null) {
			currentTask.execute();
		}
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public void uponCompletion() {
	}
}