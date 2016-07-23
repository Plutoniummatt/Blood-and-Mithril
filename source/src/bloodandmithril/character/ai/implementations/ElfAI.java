package bloodandmithril.character.ai.implementations;

import bloodandmithril.audio.SoundService.SuspicionLevel;
import bloodandmithril.character.Speech;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.routine.daily.DailyRoutine;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.individualcondition.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.ai.task.lightlightable.LightLightable;
import bloodandmithril.character.ai.task.speak.Speak;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Lightable;
import bloodandmithril.util.Util;

/**
 * AI for elves
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public final class ElfAI extends ArtificialIntelligence {
	private static final long serialVersionUID = -6956695432238102289L;


	/**
	 * Constructor
	 */
	public ElfAI(Individual host) {
		super(host);
	}


	@Override
	protected final void determineCurrentTask() {
		if (Util.roll(0.5f)) {
			wander(200f, false);
		}

		if (Util.roll(0.0005f)) {
			getHost().speak(Speech.getRandomIdleSpeech(), 2500);
		}
	}


	@Override
	protected final ArtificialIntelligence internalCopy() {
		return new ElfAI(getHost());
	}


	@Override
	public final void addRoutines() {
		healthBelowQuater();
		lightLightables();
		noiseHeard();
		morningRoutine();
	}


	private final void morningRoutine() {
		DailyRoutine dailyRoutine = new DailyRoutine(getHost().getId(), 8f, 1f);
		dailyRoutine.setAiTaskGenerator(
			new Speak.SpeakTaskGenerator(
				getHost(),
				2000,
				"Good morning!"
			)
		);
		addRoutine(dailyRoutine);
	}


	private final void noiseHeard() {
		StimulusDrivenRoutine stimRoutine = new StimulusDrivenRoutine(getHost().getId());
		stimRoutine.setTriggerFunction(new StimulusDrivenRoutine.SuspiciousSoundAITriggerFunction(SuspicionLevel.INVESTIGATE));
		stimRoutine.setAiTaskGenerator(
			new Speak.SpeakTaskGenerator(
				getHost(),
				2000,
				"What was that sound?", "Hmm?", "You hear that?", "Huh?", "What?", "I hear something..."
			)
		);
		addRoutine(stimRoutine);
	}


	private final void healthBelowQuater() {
		IndividualConditionRoutine anotherRoutine = new IndividualConditionRoutine(getHost().getId());
		anotherRoutine.setTriggerFunction(new IndividualConditionRoutine.IndividualHealthTriggerFunction(false, 25f));
		anotherRoutine.setAiTaskGenerator(
			new Speak.SpeakTaskGenerator(
				getHost(),
				2000,
				"I don't want to die!!", "Help me!!!", "Ahhhhh!!!"
			)
		);
		addRoutine(anotherRoutine);
	}


	private final void lightLightables() {
		EntityVisibleRoutine routine = new EntityVisibleRoutine(getHost().getId(), new Lightable.LightableUnlit());
		routine.setAiTaskGenerator(new LightLightable.GenerateLightAnyVisibleLightables(getHost().getId().getId()));
		addRoutine(routine);
	}
}