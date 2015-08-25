package bloodandmithril.character.ai;

import java.util.List;

import bloodandmithril.character.ai.routine.Condition;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableFunction;

import com.google.common.collect.Lists;

/**
 * A player customisable {@link AITask}, designed for automation and eliminating the need of micro-managing laborious tasks..
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Routine extends AITask {
	private static final long serialVersionUID = -8502601311459390398L;
	private int priority = 1;
	private final List<Condition> executionConditions = Lists.newLinkedList();
	private TaskFunction taskFunction;
	private AITask task;
	private String description = "";

	/**
	 * Protected constructor
	 */
	public Routine(IndividualIdentifier hostId) {
		super(hostId);
	}


	/**
	 * @return whether or not this routine should now be executed
	 */
	public boolean areExecutionConditionsMet() {
		for (Condition condition : getExecutionConditions()) {
			if (condition.met()) {
				continue;
			} else {
				return false;
			}
		}

		return true;
	}


	public void setTask(TaskFunction taskFunction) {
		this.taskFunction = taskFunction;
	}


	public int getPriority() {
		return priority;
	}


	public void setPriority(int priority) {
		this.priority = priority;
	}


	@Override
	public String getDescription() {
		return description;
	}


	/**
	 * Sets the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}


	@Override
	public boolean isComplete() {
		if (task == null) {
			if (taskFunction == null) {
				return false;
			}
			task = taskFunction.call();
		}

		return task.isComplete();
	}


	@Override
	public boolean uponCompletion() {
		if (task == null) {
			if (taskFunction == null) {
				return false;
			}
			task = taskFunction.call();
		}

		AITask toComplete = this.task;
		this.task = taskFunction.call();
		return toComplete.uponCompletion();
	}


	@Override
	public void execute(float delta) {
		if (task == null) {
			if (taskFunction == null) {
				return;
			}
			task = taskFunction.call();
		}
		task.execute(delta);
	}


	public List<Condition> getExecutionConditions() {
		return executionConditions;
	}


	public static abstract class TaskFunction implements SerializableFunction<AITask> {
		private static final long serialVersionUID = -1020689817804020435L;
	}
}