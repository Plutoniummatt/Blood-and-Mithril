package bloodandmithril.character.ai.task.wait;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * {@link AITask} that instructs the {@link Individual} to wait for a specified amount of time
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ExecutedBy(WaitExecutor.class)
public class Wait extends AITask {
	private static final long serialVersionUID = 1131002096994485862L;

	/** Time to wait */
	float time;

	/** Used for processing */
	long systemTimeSinceLastUpdate = System.currentTimeMillis();

	/** {@link #execute()} will set this to true when the {@link #time} expires */
	boolean complete = false;

	/**
	 * Constructor
	 */
	public Wait(final Individual host, final float time) {
		super(host.getId());
		this.time = time;
	}

	@Override
	public String getShortDescription() {
		return "Waiting";
	}
}