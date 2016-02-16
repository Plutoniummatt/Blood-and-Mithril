package bloodandmithril.character.ai;

import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.core.Copyright;

/**
 * Provider of the next waypoint
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface NextWaypointProvider {

	/**
	 * @return the next {@link WayPoint}
	 */
	public WayPoint provideNextWaypoint();
}
