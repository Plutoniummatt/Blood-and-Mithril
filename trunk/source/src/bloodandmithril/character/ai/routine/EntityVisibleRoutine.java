package bloodandmithril.character.ai.routine;

import java.util.List;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.Domain;

/**
 * A {@link Routine} that depends on the outcome of a {@link Condition} to trigger the following tasks
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class EntityVisibleRoutine<T extends Visible> extends Routine {
	private static final long serialVersionUID = -5762591639048417273L;

	private SerializableMappingFunction<T, Boolean> identificationFunction;
	private SerializableMappingFunction<T, AITask> aiTaskGenerator;
	private AITask task;
	private Class<T> tClass;

	/**
	 * Constructor
	 */
	public EntityVisibleRoutine(IndividualIdentifier hostId, Class<T> tClass, SerializableMappingFunction<T, Boolean> identificationFunction) {
		super(hostId);
		this.tClass = tClass;
		this.identificationFunction = identificationFunction;
		setDescription("Entity visible routine");
	}


	@Override
	@SuppressWarnings("unchecked")
	public boolean areExecutionConditionsMet() {
		Individual individual = Domain.getIndividual(hostId.getId());
		List<Visible> observed = ((Observer) individual).observe(individual.getWorldId(), individual.getId().getId());
		for (Visible v : observed) {
			if (tClass.isAssignableFrom(v.getClass()) && identificationFunction.apply((T) v)) {
				return true;
			}
		}

		return false;
	}


	/**
	 * @return the visible entity, or null if nothing is visible
	 */
	@SuppressWarnings("unchecked")
	public T getVisibleEntity() {
		Individual individual = Domain.getIndividual(hostId.getId());
		List<Visible> observed = ((Observer) individual).observe(individual.getWorldId(), individual.getId().getId());
		for (Visible v : observed) {
			if (tClass.isAssignableFrom(v.getClass()) && identificationFunction.apply((T) v)) {
				return (T) v;
			}
		}

		return null;
	}


	public void setAiTaskGenerator(SerializableMappingFunction<T, AITask> aiTaskGenerator) {
		this.aiTaskGenerator = aiTaskGenerator;
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


	@Override
	public void prepare() {
		this.task = aiTaskGenerator.apply(getVisibleEntity());
	}
}
