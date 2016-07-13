package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * An {@link AITask} that tells the {@link Individual} to herp derp do nothing.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Idle extends AITask {
	private static final long serialVersionUID = 6303096918027910962L;

	/**
	 * @param hostId
	 */
	public Idle() {
		super(null);
	}


	@Override
	protected void internalExecute(final float delta) {
		//Do Nothing
	}


	@Override
	public String getShortDescription() {
		return "Idle";
	}


	@Override
	public boolean isComplete() {
		return true;
	}


	@Override
	public boolean uponCompletion() {
		return false;
	}
}
