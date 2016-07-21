package bloodandmithril.character.ai;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableMappingFunction;

@Copyright("Matthew Peck 2016")
public abstract class TaskGenerator extends SerializableMappingFunction<Object, AITask> {
	private static final long serialVersionUID = 229150218799462799L;

	/**
	 * @return the detailed description of the task this generator generates
	 */
	public abstract String getDailyRoutineDetailedDescription();

	/**
	 * @return the detailed description of the task this generator generates
	 */
	public abstract String getEntityVisibleRoutineDetailedDescription();

	/**
	 * @return the detailed description of the task this generator generates
	 */
	public abstract String getIndividualConditionRoutineDetailedDescription();

	/**
	 * @return the detailed description of the task this generator generates
	 */
	public abstract String getStimulusDrivenRoutineDetailedDescription();

	/**
	 * @return true if this {@link TaskGenerator} can generate a valid {@link RoutineTask} for a {@link Routine}
	 */
	public abstract boolean valid();

	/**
	 * Renders the visual aide to indicate entities involved in the {@link RoutineTask}
	 */
	public abstract void render();
}