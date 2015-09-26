package bloodandmithril.character.ai;

import bloodandmithril.util.SerializableMappingFunction;

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
}