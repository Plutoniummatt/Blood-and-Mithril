package bloodandmithril.character.ai.routine;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;

/**
 * {@link Routine} that executes at, or later than a specified time every day
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class DailyRoutine extends Routine<Individual> {
	private static final long serialVersionUID = -255141692263126217L;

	private SerializableMappingFunction<Individual, AITask> aiTaskGenerator;
	private int lastExecutedDayOfMonth = 99;
	private float routineTime;
	private AITask task;

	/**
	 * Constructor
	 */
	public DailyRoutine(IndividualIdentifier hostId, float routineTime) {
		super(hostId);
		this.routineTime = routineTime;
		setDescription("Daily routine");
	}


	@Override
	public void setAiTaskGenerator(SerializableMappingFunction<Individual, AITask> aiTaskGenerator) {
		this.aiTaskGenerator = aiTaskGenerator;
	}


	@Override
	public boolean areExecutionConditionsMet() {
		Epoch currentEpoch = Domain.getWorld(getHost().getWorldId()).getEpoch();
		return currentEpoch.getTime() >= routineTime && currentEpoch.dayOfMonth != lastExecutedDayOfMonth;
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
			this.lastExecutedDayOfMonth = Domain.getWorld(getHost().getWorldId()).getEpoch().dayOfMonth;
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