package bloodandmithril.character.ai.task.idle;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * An {@link AITask} that tells the {@link Individual} to herp derp do nothing.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ExecutedBy(IdleExecutor.class)
public class Idle extends AITask {
	private static final long serialVersionUID = 6303096918027910962L;

	/**
	 * @param hostId
	 */
	public Idle() {
		super(null);
	}


	@Override
	public String getShortDescription() {
		return "Idle";
	}
}
