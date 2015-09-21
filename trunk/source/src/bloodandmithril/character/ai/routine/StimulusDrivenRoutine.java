package bloodandmithril.character.ai.routine;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.perception.SoundStimulus;
import bloodandmithril.character.ai.perception.Stimulus;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableMappingFunction;

/**
 * A {@link Routine} that is triggered by a {@link Stimulus} such as {@link SoundStimulus}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class StimulusDrivenRoutine extends Routine<Stimulus> {
	private static final long serialVersionUID = 2347934053852793343L;

	private SerializableMappingFunction<Stimulus, Boolean> triggerFunction;
	private SerializableMappingFunction<Stimulus, AITask> aiTaskGenerator;
	private Stimulus triggeringStimulus;
	private boolean triggered;
	private AITask task;

	/**
	 * Constructor
	 */
	public StimulusDrivenRoutine(IndividualIdentifier hostId, SerializableMappingFunction<Stimulus, Boolean> triggerFunction) {
		super(hostId);
		this.triggerFunction = triggerFunction;
		setDescription("Stimulus driven routine");
	}


	/**
	 * Attempt to trigger the execution of this {@link Routine}
	 */
	public void attemptTrigger(Stimulus stimulus) {
		if (triggerFunction.apply(stimulus)) {
			this.triggeringStimulus = stimulus;
			this.triggered = true;
		}
	}


	/**
	 * Sets the generator for the task which this routine will use to generate the {@link AITask} upon meeting the trigger function
	 */
	@Override
	public void setAiTaskGenerator(SerializableMappingFunction<Stimulus, AITask> aiTaskGenerator) {
		this.aiTaskGenerator = aiTaskGenerator;
	}


	@Override
	public boolean areExecutionConditionsMet() {
		return triggered;
	}


	@Override
	public void prepare() {
		this.task = aiTaskGenerator.apply(triggeringStimulus);
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
			this.triggered = false;
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