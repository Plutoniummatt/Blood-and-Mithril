package bloodandmithril.character.ai.routine;

import java.util.List;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.CompositeAITask;
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
	private List<SerializableFunction<Boolean>> executionConditions = Lists.newLinkedList();
	private CompositeAITask task;
	private String description = "";

	/**
	 * Protected constructor
	 */
	protected Routine(IndividualIdentifier hostId) {
		super(hostId);
	}


	/**
	 * @return whether or not this routine should now be executed
	 */
	public boolean areExecutionConditionsMet() {
		for (SerializableFunction<Boolean> condition : executionConditions) {
			if (condition.call()) {
				continue;
			} else {
				return false;
			}
		}

		return true;
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


	@Override
	public boolean isComplete() {
		return task.isComplete();
	}


	@Override
	public boolean uponCompletion() {
		return task.uponCompletion();
	}


	@Override
	public void execute(float delta) {
		task.execute(delta);
	}
}