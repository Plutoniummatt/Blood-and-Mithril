package bloodandmithril.character.ai.implementations;

import bloodandmithril.audio.SoundService;
import bloodandmithril.audio.SoundService.SuspicionLevel;
import bloodandmithril.audio.SoundService.SuspiciousSound;
import bloodandmithril.character.Speech;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.routine.DailyRoutine;
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
		morningRoutine();
	}


	private void morningRoutine() {
		DailyRoutine dailyRoutine = new DailyRoutine(getHost().getId(), 8, 1);
		dailyRoutine.setAiTaskGenerator(
			new Speak.SpeakTaskGenerator<Individual>(
				getHost(),
				2000,
				"Good morning!"
			)
		);

		addRoutine(dailyRoutine);
	}


	private void noiseHeard() {
		StimulusDrivenRoutine<SuspiciousSound> stimRoutine = new StimulusDrivenRoutine<SuspiciousSound>(
			getHost().getId(),
			new SoundService.SuspiciousSoundAITriggerFunction(SuspicionLevel.INVESTIGATE),
			SuspiciousSound.class
		);

		stimRoutine.setAiTaskGenerator(
			new Speak.SpeakTaskGenerator<SuspiciousSound>(
				getHost(),
				2000,
				"What was that sound?", "Hmm?", "You hear that?", "Huh?", "What?", "I hear something..."
			)
		);

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

		anotherRoutine.setAiTaskGenerator(
			new Speak.SpeakTaskGenerator<Individual>(
				getHost(),
				2000,
				"I don't want to die!!", "Help me!!!", "Ahhhhh!!!"
			)
		);

		addRoutine(anotherRoutine);
	}


	private void lightLightables() {
		EntityVisibleRoutine routine = new EntityVisibleRoutine(getHost().getId(), Lightable.class, new Lightable.LightableUnlit());
		routine.setAiTaskGenerator(new LightLightable.GenerateLightAnyVisibleLightables(getHost().getId().getId()));

		addRoutine(routine);
	}
}