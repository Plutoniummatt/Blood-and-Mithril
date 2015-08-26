package bloodandmithril.character.ai.implementations;

import bloodandmithril.character.Speech;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.routine.ConditionChainedRoutine;
import bloodandmithril.character.ai.routine.condition.EntityVisible;
import bloodandmithril.character.ai.routine.condition.EntityVisible.IsSuperClassFunction;
import bloodandmithril.character.ai.routine.condition.LightableUnlit;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.ai.task.LightLightable;
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
		ConditionChainedRoutine<Visible> routine = new ConditionChainedRoutine<Visible>(getHost().getId());
		routine.setDescription("Lightable lighting routine");
		EntityVisible entityVisible = new EntityVisible(
			new IsSuperClassFunction(Lightable.class),
			(Observer) getHost()
		);
		routine.getConditions().add(entityVisible);
		routine.getConditions().add(
			new LightableUnlit(entityVisible)
		);
		routine.setEntityGenerator(entityVisible);
		routine.setTaskGenerator(() -> {
			if (getHost().getFireLighter() == null) {
				return new Idle();
			}
			
			try {
				return new LightLightable(getHost(), (Lightable)entityVisible.call(), true);
			} catch (Exception e) {
				e.printStackTrace();
				return new Idle();
			}
		});
		addRoutine(routine);
	}
}