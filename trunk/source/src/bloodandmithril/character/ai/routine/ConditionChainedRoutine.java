package bloodandmithril.character.ai.routine;

import java.io.Serializable;
import java.util.List;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableFunction;

import com.google.common.collect.Lists;

/**
 * A {@link Routine} that depends on the outcome of a {@link Condition} to trigger the following tasks
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ConditionChainedRoutine<T extends Serializable> extends Routine {
	private static final long serialVersionUID = -5762591639048417273L;

	private List<Condition> conditions = Lists.newLinkedList();
	private SerializableFunction<T> entityGenerator;
	private SerializableFunction<AITask> taskGenerator;
	private AITask task;
	private T entity;
	
	/**
	 * Constructor
	 */
	public ConditionChainedRoutine(IndividualIdentifier hostId) {
		super(hostId);
	}
	
	
	public void setEntityGenerator(SerializableFunction<T> entityGenerator) {
		this.entityGenerator = entityGenerator;
	}
	
	
	public void setTaskGenerator(SerializableFunction<AITask> taskGenerator) {
		this.taskGenerator = taskGenerator;
	}
	
	
	public List<Condition> getConditions() {
		return conditions;
	}
	
	
	public AITask generateTask() {
		AITask generated = taskGenerator.call();
		this.task = generated;
		return generated;
	}
	
	
	public T generateEntity() {
		this.entity = entityGenerator.call();
		return entity;
	}

	
	@Override
	public boolean areExecutionConditionsMet() {
		for (Condition c : conditions) {
			if (!c.met()) {
				return false;
			}
		}
		
		return true;
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
		
		AITask local = task;
		this.task = null;
		return local.uponCompletion();
	}

	
	@Override
	public void execute(float delta) {
		if (task == null) {
			return;
		}
		
		task.execute(delta);
	}
}
