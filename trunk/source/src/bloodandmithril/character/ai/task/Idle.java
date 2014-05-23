package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;

/**
 * An {@link AITask} that tells the {@link Individual} to herp derp do nothing.
 *
 * @author Matt
 */
public class Idle extends AITask {
	private static final long serialVersionUID = 6303096918027910962L;

	/**
	 * @param hostId
	 */
	public Idle() {
		super(null);
	}


	@Override
	public void execute(float delta) {
		//Do Nothing
	}


	@Override
	public String getDescription() {
		return "Idle";
	}


	@Override
	public boolean isComplete() {
		return true;
	}


	@Override
	public void uponCompletion() {
	}
}
