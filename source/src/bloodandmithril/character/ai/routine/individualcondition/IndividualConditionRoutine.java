package bloodandmithril.character.ai.routine.individualcondition;

import java.util.ArrayDeque;
import java.util.Deque;

import com.google.common.collect.Iterables;

import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.conditions.Condition;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.EditAIRoutineWindow;
import bloodandmithril.util.SerializableMappingFunction;

/**
 * A Routine based on the condition of the host
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@ExecutedBy(IndividualConditionExecutor.class)
public final class IndividualConditionRoutine extends Routine {
	private static final long serialVersionUID = 6831994593107089893L;

	IndividualConditionTriggerFunction executionCondition;

	/**
	 * Constructor
	 */
	public IndividualConditionRoutine(final IndividualIdentifier hostId) {
		super(hostId);
		setDescription("Condition routine");
	}


	public final void setTriggerFunction(final IndividualConditionTriggerFunction executionCondition) {
		this.executionCondition = executionCondition;
	}


	@Override
	public final Object getTaskGenerationParameter() {
		return getHost();
	}


	@Override
	public final boolean areExecutionConditionsMet() {
		return executionCondition.apply(getHost());
	}


	@Override
	public final Deque<Panel> constructEditWizard(final EditAIRoutineWindow parent) {
		final Deque<Panel> wizard = new ArrayDeque<>();
		wizard.add(new IndividualConditionRoutinePanel(parent, this));
		return wizard;
	}


	public static abstract class IndividualConditionTriggerFunction extends SerializableMappingFunction<Individual, Boolean> {
		private static final long serialVersionUID = -7651195239417056155L;
		public abstract String getDetailedDescription(Individual host);
	}


	public static final class IndividualAffectedByConditionTriggerFunction extends IndividualConditionTriggerFunction {
		private static final long serialVersionUID = -4307447978296098496L;
		private Class<? extends Condition> condition;

		public IndividualAffectedByConditionTriggerFunction(final Class<? extends Condition> condition) {
			this.condition = condition;
		}

		@Override
		public final Boolean apply(final Individual input) {
			return Iterables.tryFind(input.getState().currentConditions, c -> {
				return condition.isAssignableFrom(c.getClass());
			}).isPresent();
		}
		@Override
		public final String getDetailedDescription(final Individual host) {
			return "This routine occurs when affected by " + condition.getAnnotation(Name.class).name();
		}
	}


	public static final class IndividualHealthTriggerFunction extends IndividualConditionTriggerFunction {
		private static final long serialVersionUID = -676643881949925314L;
		private boolean greaterThan;
		private float percentage;

		public IndividualHealthTriggerFunction(final boolean greaterThan, final float percentage) {
			this.greaterThan = greaterThan;
			if (percentage < 0 || percentage > 100) {
				throw new RuntimeException();
			}
			this.percentage = percentage / 100f;
		}

		@Override
		public final Boolean apply(final Individual input) {
			if (greaterThan) {
				return input.getState().health/input.getState().maxHealth > percentage;
			} else {
				return input.getState().health/input.getState().maxHealth < percentage;
			}
		}
		@Override
		public final String getDetailedDescription(final Individual host) {
			if (greaterThan) {
				return "This routine occurs when health is above " + String.format("%.2f", percentage*100) + "%";
			} else {
				return "This routine occurs when health is below " + String.format("%.2f", percentage*100) + "%";
			}
		}
	}
}