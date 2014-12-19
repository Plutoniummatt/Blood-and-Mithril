package bloodandmithril.character.ai.task;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.ai.AIProcessor.JitGoToLocation;
import bloodandmithril.character.ai.AITask;
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
	public void addGotoLocation(JitGoToLocation goToLocation) {
		appendTask(goToLocation);
	}


	/**
	 * Adds a {@link GoToLocation} task
	 */
	public void addJump(Jump jump) {
		appendTask(jump);
	}
	
	
	public Vector2 getDestination() {
		AITask task = getCurrentTask();
		if (task instanceof GoToLocation) {
			return ((GoToLocation) task).getPath().getDestinationWayPoint().waypoint.cpy();
		} else {
			return ((Jump) task).getDestination();
		}
	}
}