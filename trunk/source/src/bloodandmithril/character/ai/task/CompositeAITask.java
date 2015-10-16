package bloodandmithril.character.ai.task;

import java.util.ArrayDeque;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;

/**
 * An {@link AITask} that is a composite of multiple {@link AITask}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
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
	protected CompositeAITask(IndividualIdentifier hostId, String description, AITask... tasks) {
		super(hostId);
		this.description = description;

		for (AITask task : tasks) {
			this.tasks.addLast(task);
		}

		setCurrentTask(this.tasks.poll());
	}


	/** Adds a task to the end of the queue */
	protected final void appendTask(AITask taskToAdd) {
		tasks.addLast(taskToAdd);

		if (getCurrentTask() == null) {
			setCurrentTask(tasks.poll());
		}
	}


	/** Adds a task to the front of the queue */
	protected final void setNextTask(AITask taskToAdd) {
		tasks.addFirst(taskToAdd);
	}


	/**
	 * @see bloodandmithril.character.ai.AITask#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return getCurrentTask() == null;
	}


	/**
	 * @see bloodandmithril.character.ai.AITask#execute()
	 */
	@Override
	public final void execute(float delta) {
		if (getCurrentTask() == null) {
			return;
		}

		if (getCurrentTask().isComplete()) {
			getCurrentTask().uponCompletion();
			setCurrentTask(tasks.poll());
		}

		if (getCurrentTask() != null) {
			getCurrentTask().execute(delta);
		}
	}


	@Override
	public String getShortDescription() {
		return description;
	}


	@Override
	public boolean uponCompletion() {
		return false;
	}


	public AITask getCurrentTask() {
		return currentTask;
	}


	public void setCurrentTask(AITask currentTask) {
		this.currentTask = currentTask;
	}
}