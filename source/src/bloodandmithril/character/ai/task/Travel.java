package bloodandmithril.character.ai.task;

import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;

/**
 * A {@link Travel} task is an ordered series of {@link GoToLocation}s and {@link Jump}s that get executed sequentially
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Travel extends CompositeAITask {
	private static final long serialVersionUID = -1118542666642761349L;

	/**
	 * Constructor
	 */
	public Travel(IndividualIdentifier hostId) {
		super(hostId, "Travelling");
	}


	/**
	 * Adds a {@link GoToLocation} task
	 */
	public void addGotoLocation(GoToLocation goToLocation) {
		appendTask(goToLocation);
	}


	/**
	 * Adds a {@link GoToLocation} task
	 */
	public void addJump(Jump jump) {
		appendTask(jump);
	}
}