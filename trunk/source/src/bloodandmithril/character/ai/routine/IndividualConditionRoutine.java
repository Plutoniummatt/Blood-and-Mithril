package bloodandmithril.character.ai.routine;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableMappingFunction;

/**
 * A Routine based on the condition of the host
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualConditionRoutine extends Routine {
	private static final long serialVersionUID = 6831994593107089893L;
	private SerializableMappingFunction<Individual, Boolean> executionCondition;
	private SerializableMappingFunction<Individual, AITask> aiTaskGenerator;
	private AITask task;

	/**
	 * Constructor
	 */
	public IndividualConditionRoutine(IndividualIdentifier hostId, SerializableMappingFunction<Individual, Boolean> executionCondition) {
		super(hostId);
		this.executionCondition = executionCondition;
		setDescription("Condition routine");
	}


	public void setAiTaskGenerator(SerializableMappingFunction<Individual, AITask> aiTaskGenerator) {
		this.aiTaskGenerator = aiTaskGenerator;
	}


	@Override
	public boolean areExecutionConditionsMet() {
		return executionCondition.apply(getHost());
	}


	@Override
	public void prepare() {
		this.task = aiTaskGenerator.apply(getHost());
	}


	@Override
	public boolean isComplete() {
		if (task != null) {
			return task.isComplete();
		}

		return false;
	}


	@Override
	public boolean uponCompletion() {
		if (task != null) {
			AITask toNullify = task;
			this.task = null;
			return toNullify.uponCompletion();
		}

		return false;
	}


	@Override
	public void execute(float delta) {
		if (task != null) {
			task.execute(delta);
		}
	}
}