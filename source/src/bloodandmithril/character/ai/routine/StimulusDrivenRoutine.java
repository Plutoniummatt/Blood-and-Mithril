package bloodandmithril.character.ai.routine;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.perception.SoundStimulus;
import bloodandmithril.character.ai.perception.Stimulus;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;

/**
 * A {@link Routine} that is triggered by a {@link Stimulus} such as {@link SoundStimulus}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class StimulusDrivenRoutine extends Routine {
	private static final long serialVersionUID = 2347934053852793343L;

	/**
	 * Constructor
	 */
	public StimulusDrivenRoutine(IndividualIdentifier hostId) {
		super(hostId);
	}


	@Override
	public boolean areExecutionConditionsMet() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void prepare() {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean isComplete() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean uponCompletion() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void execute(float delta) {
		// TODO Auto-generated method stub
	}
}