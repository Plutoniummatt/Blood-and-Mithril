package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableFunction;

/**
 * A Jit version of {@link AITask}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class JitAITask extends AITask {
	private static final long serialVersionUID = 50973528081693772L;
	private SerializableFunction<? extends AITask> taskFunction;
	private AITask task;
	
	/**
	 * Constructor
	 */
	public JitAITask(IndividualIdentifier hostId, SerializableFunction<? extends AITask> taskFunction) {
		super(hostId);
		this.taskFunction = taskFunction;
	}
	
	
	public AITask getTask() {
		return task;
	}
	
	
	public void initialise() {
		if (task == null) {
			this.task = taskFunction.call();
		}
	}
	

	@Override
	public String getDescription() {
		initialise();
		return task.getDescription();
	}

	
	@Override
	public boolean isComplete() {
		initialise();
		return task.isComplete();
	}

	
	@Override
	public boolean uponCompletion() {
		initialise();
		return task.uponCompletion();
	}

	
	@Override
	public void execute(float delta) {
		initialise();
		task.execute(delta);
	}
}