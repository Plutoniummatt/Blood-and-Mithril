package bloodandmithril.character.ai.implementations;

import bloodandmithril.audio.SoundService.SuspicionLevel;
import bloodandmithril.audio.SoundService.SuspiciousSound;
import bloodandmithril.character.Speech;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.perception.Stimulus;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.ai.task.LightLightable;
import bloodandmithril.character.ai.task.Speak;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Lightable;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * AI for elves
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ElfAI extends ArtificialIntelligence {
	private static final long serialVersionUID = -6956695432238102289L;


	/**
	 * Constructor
	 */
	public ElfAI(Individual host) {
		super(host);
	}


	@Override
	protected void determineCurrentTask() {
		if (Util.roll(0.5f)) {
			wander(200f, false);
		}

		if (Util.roll(0.0005f)) {
			getHost().speak(Speech.getRandomIdleSpeech(), 2500);
		}
	}


	@Override
	protected ArtificialIntelligence internalCopy() {
		return new ElfAI(getHost());
	}


	@Override
	public void addRoutines() {
		healthBelowQuater();
		lightLightables();
		noiseHeard();
	}


	private void noiseHeard() {
		StimulusDrivenRoutine stimRoutine = new StimulusDrivenRoutine(getHost().getId(), new SerializableMappingFunction<Stimulus, Boolean>() {
			private static final long serialVersionUID = 1314379365402151840L;
			@Override
			public Boolean apply(Stimulus input) {
				if (input instanceof SuspiciousSound) {
					return true;
				}

				return false;
			}
		});

		stimRoutine.setAiTaskGenerator(new SerializableMappingFunction<Stimulus, AITask>() {
			private static final long serialVersionUID = -300681122490177105L;
			@Override
			public AITask apply(Stimulus input) {
				if (input instanceof SuspiciousSound) {
					SuspicionLevel suspicionLevel = ((SuspiciousSound) input).getSuspicionLevel();
					if (suspicionLevel.severity >= SuspicionLevel.INVESTIGATE.severity) {
						String speech = Util.randomOneOf("What was that sound?", "Hmm?", "You hear that?", "Huh?", "What?", "I hear something...");

						return new Speak(getHost(), speech, 1500);
					}
				}

				return null;
			}
		});

		addRoutine(stimRoutine);
	}


	private void healthBelowQuater() {
		IndividualConditionRoutine anotherRoutine = new IndividualConditionRoutine(getHost().getId(), new SerializableMappingFunction<Individual, Boolean>() {
			private static final long serialVersionUID = -8900729069395449472L;
			@Override
			public Boolean apply(Individual input) {
				return input.getState().health/input.getState().maxHealth < 0.25f;
			}
		});

		anotherRoutine.setAiTaskGenerator(new SerializableMappingFunction<Individual, AITask>() {
			private static final long serialVersionUID = 372181266426810689L;
			@Override
			public AITask apply(Individual input) {
				return new Speak(getHost(), "Fuck, I'm gonna die!", 2000);
			}
		});

		addRoutine(anotherRoutine);
	}


	private void lightLightables() {
		EntityVisibleRoutine<Lightable> routine = new EntityVisibleRoutine<Lightable>(getHost().getId(), Lightable.class, new SerializableMappingFunction<Lightable, Boolean>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Boolean apply(Lightable input) {
				return !input.isLit();
			}
		});

		routine.setAiTaskGenerator(new SerializableMappingFunction<Lightable, AITask>() {
			private static final long serialVersionUID = 4879197288910331133L;
			@Override
			public AITask apply(Lightable input) {
				try {
					if (getHost().getFireLighter() == null) {
						return null;
					}
					return new LightLightable(getHost(), input, true);
				} catch (NoTileFoundException e) {
					return null;
				}
			}
		});

		addRoutine(routine);
	}
}