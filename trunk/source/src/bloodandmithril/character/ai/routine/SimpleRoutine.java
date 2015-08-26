package bloodandmithril.character.ai.routine;

import java.util.List;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.util.SerializableFunction;

import com.google.common.collect.Lists;

/**
 * A simple {@link Routine} that has a set of execution conditions and an {@link AITask} to execute
 *
 * @author Matt
 */
public class SimpleRoutine extends Routine {
	private static final long serialVersionUID = -3817282717992483267L;
	
	private final List<Condition> executionConditions = Lists.newLinkedList();
	private SerializableFunction<AITask> taskGenerator;
	private AITask task;
	
	/**
	 * Constructor
	 */
	public SimpleRoutine(IndividualIdentifier hostId) {
		super(hostId);
	}

	
	@Override
	public boolean areExecutionConditionsMet() {
		for (Condition c : executionConditions) {
			if (!c.met()) {
				return false;
			}
		}
		
		return true;
	}
	
	
	public void setTaskGenerator(SerializableFunction<AITask> taskGenerator) {
		this.taskGenerator = taskGenerator;
	}

	
	public void generateTask() {
		this.task = taskGenerator.call();
	}
	
	
	@Override
	public boolean isComplete() {
		if (task == null) {
			return false;
		}
		return task.isComplete();
	}

	
	@Override
	public boolean uponCompletion() {
		if (task == null) {
			return false;
		}
		return task.uponCompletion();
	}

	
	@Override
	public void execute(float delta) {
		if (task == null) {
			return;
		}
		task.execute(delta);
	}
}
