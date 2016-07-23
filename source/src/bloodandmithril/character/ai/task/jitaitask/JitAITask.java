package bloodandmithril.character.ai.task.jitaitask;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableFunction;

/**
 * A Jit version of {@link AITask}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ExecutedBy(JitAITaskExecutor.class)
public class JitAITask extends AITask {
	private static final long serialVersionUID = 50973528081693772L;
	private SerializableFunction<? extends AITask> taskFunction;
	AITask task;

	/**
	 * Constructor
	 */
	public JitAITask(final IndividualIdentifier hostId, final SerializableFunction<? extends AITask> taskFunction) {
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
	public String getShortDescription() {
		initialise();
		return task.getShortDescription();
	}
}