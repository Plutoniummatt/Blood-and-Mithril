package bloodandmithril.character.ai.routine;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.TaskGenerator;
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
public class StimulusDrivenRoutine<S extends Stimulus> extends Routine<S> {
	private static final long serialVersionUID = 2347934053852793343L;

	private SerializableMappingFunction<S, Boolean> triggerFunction;
	private TaskGenerator<S> aiTaskGenerator;
	private S triggeringStimulus;
	private boolean triggered;
	private AITask task;
	private Class<S> c;

	/**
	 * Constructor
	 */
	public StimulusDrivenRoutine(IndividualIdentifier hostId, SerializableMappingFunction<S, Boolean> triggerFunction, Class<S> c) {
		super(hostId);
		this.triggerFunction = triggerFunction;
		this.c = c;
		setDescription("Stimulus driven routine");
	}


	/**
	 * Attempt to trigger the execution of this {@link Routine}
	 */
	public void attemptTrigger(Stimulus stimulus) {
		if (stimulus.getClass().equals(c)) {
			if (triggerFunction.apply(c.cast(stimulus))) {
				this.triggeringStimulus = c.cast(stimulus);
				this.triggered = true;
			}
		}
	}


	/**
	 * Sets the generator for the task which this routine will use to generate the {@link AITask} upon meeting the trigger function
	 */
	@Override
	public void setAiTaskGenerator(TaskGenerator<S> aiTaskGenerator) {
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