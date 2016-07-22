package bloodandmithril.character.ai.routine.stimulusdriven;

import java.util.ArrayDeque;
import java.util.Deque;

import bloodandmithril.audio.SoundService.SuspicionLevel;
import bloodandmithril.audio.SoundService.SuspiciousSound;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.perception.SoundStimulus;
import bloodandmithril.character.ai.perception.Stimulus;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.EditAIRoutineWindow;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.SerializableMappingFunction;

/**
 * A {@link Routine} that is triggered by a {@link Stimulus} such as {@link SoundStimulus}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@ExecutedBy(StimulusDrivenExecutor.class)
public final class StimulusDrivenRoutine extends Routine {
	private static final long serialVersionUID = 2347934053852793343L;

	StimulusTriggerFunction triggerFunction;
	Stimulus triggeringStimulus;
	boolean triggered;

	/**
	 * Constructor
	 */
	public StimulusDrivenRoutine(final IndividualIdentifier hostId) {
		super(hostId);
		setDescription("Stimulus driven routine");
	}


	public final void setTriggerFunction(final StimulusTriggerFunction triggerFunction) {
		this.triggerFunction = triggerFunction;
	}


	/**
	 * Attempt to trigger the execution of this {@link Routine}
	 */
	public final void attemptTrigger(final Stimulus stimulus) {
		if (triggerFunction.s.isAssignableFrom(stimulus.getClass())) {
			if (triggerFunction.apply(stimulus)) {
				this.triggeringStimulus = stimulus;
				this.triggered = true;
			}
		}
	}


	public final StimulusTriggerFunction getTriggerFunction() {
		return triggerFunction;
	}


	/**
	 * @return the stimulus that triggered this routine
	 */
	public SerializableFunction<Stimulus> getTriggeringStimulus() {
		return new TriggeringStimulusFuture();
	}


	public class TriggeringStimulusFuture implements SerializableFunction<Stimulus> {
		private static final long serialVersionUID = 8196898951457294073L;
		@Override
		public Stimulus call() {
			return triggeringStimulus;
		}
	}


	@Override
	public final Object getTaskGenerationParameter() {
		return triggeringStimulus;
	}


	@Override
	public final boolean areExecutionConditionsMet() {
		return triggered;
	}


	@Override
	public final Deque<Panel> constructEditWizard(final EditAIRoutineWindow parent) {
		final Deque<Panel> wizard = new ArrayDeque<>();
		wizard.add(new StimulusDrivenRoutinePanel(parent, this));
		return wizard;
	}


	public static abstract class StimulusTriggerFunction extends SerializableMappingFunction<Stimulus, Boolean> {
		private static final long serialVersionUID = 6829023381484228088L;
		private Class<? extends Stimulus> s;

		protected StimulusTriggerFunction(final Class<? extends Stimulus> s) {
			this.s = s;
		}

		public Class<? extends Stimulus> getTriggeringStimulusClass() {
			return s;
		}

		public abstract String getDetailedDescription();
	}


	public static final class SuspiciousSoundAITriggerFunction extends StimulusTriggerFunction {
		private static final long serialVersionUID = 5189638348993362947L;
		private SuspicionLevel level;

		public SuspiciousSoundAITriggerFunction(final SuspicionLevel level) {
			super(SuspiciousSound.class);
			this.level = level;
		}

		@Override
		public final String getDetailedDescription() {
			return "This event occurs when a suspicious sound is heard";
		}

		@Override
		public final Boolean apply(final Stimulus input) {
			if (input instanceof SuspiciousSound) {
				return ((SuspiciousSound) input).getSuspicionLevel().severity >= level.severity;
			}

			return false;
		}
	}
}