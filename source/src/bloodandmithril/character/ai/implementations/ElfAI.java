package bloodandmithril.character.ai.implementations;

import bloodandmithril.character.Speech;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.task.LightLightable;
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
					return new LightLightable(getHost(), input, true);
				} catch (NoTileFoundException e) {
					return null;
				}
			}
		});
	}
}