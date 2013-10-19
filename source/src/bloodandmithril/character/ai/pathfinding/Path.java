package bloodandmithril.character.ai.pathfinding;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.TreeMap;

import bloodandmithril.Fortress;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;


/**
 * A Path for {@link ArtificialIntelligence} to follow
 *
 * @author Matt
 */
public class Path implements Serializable {
	private static final long serialVersionUID = -2569430046328226956L;
	
	/** {@link WayPoint}s associated with this {@link Path} */
	private TreeMap<Integer, WayPoint> waypoints = new TreeMap<Integer, WayPoint>();
	
	/**
	 * Constructor
	 */
	public Path(WayPoint... wayPoints) {
		int i = 0;
		for (WayPoint wayPoint : wayPoints) {
			if (wayPoint.waypoint != null) {
				waypoints.put(i, wayPoint);
			}
			i++;
		}
	}
	
	
	/**
	 * True if location is part of this {@link Path}
	 */
	public synchronized boolean isPartOfPathGroundAndIsNext(Vector2 location) {
		Vector2 flooredCoords = Topography.convertToWorldCoord(location, true);
		if (!waypoints.isEmpty() && waypoints.firstEntry().getValue().waypoint.equals(flooredCoords)) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * True if location is directly above the next waypoing in the {@link Path}
	 */
	public synchronized boolean isDirectlyAboveNext(Vector2 location) {
		Vector2 flooredCoords = Topography.convertToWorldCoord(location, true);
		if (location.y < 0) {
			flooredCoords = Topography.convertToWorldCoord(location.x, location.y + 1, true);	
		}
		if (!waypoints.isEmpty() && 
			waypoints.firstEntry().getValue().waypoint.x == flooredCoords.x && 
			waypoints.firstEntry().getValue().waypoint.y < flooredCoords.y) {
			return true;
		}
		return false;
	}
	
	
	/** Adds a {@link WayPoint} to this {@link Path} */
	public synchronized void addWayPoint(WayPoint wayPoint) {
		waypoints.put(waypoints.lastEntry() == null ? 1 : waypoints.lastEntry().getKey() + 1, wayPoint);
	}
	
	
	/** Adds a {@link WayPoint} to this {@link Path} at the beginning */
	public synchronized void addWayPointReversed(WayPoint wayPoint) {
		waypoints.put(waypoints.isEmpty() ? 1 : waypoints.firstKey() - 1, wayPoint);
	}
	
	
	/** Clears all {@link WayPoint}s of this {@link Path} */
	public synchronized void clear() {
		waypoints.clear();
	}
	
	
	/**
	 * Renders all the waypoint of this {@link Path}
	 */
	public void render() {
		TreeMap<Integer, WayPoint> waypointsCopy = new TreeMap<Integer, WayPoint>(waypoints);
		for (Entry<Integer, WayPoint> entry : waypointsCopy.entrySet()) {
			float x = Fortress.worldToScreenX(entry.getValue().waypoint.x);
			float y = Fortress.worldToScreenY(entry.getValue().waypoint.y);
			
			UserInterface.shapeRenderer.begin(ShapeType.FilledCircle);
			UserInterface.shapeRenderer.filledCircle(x, y, 3);
			UserInterface.shapeRenderer.end();
			
			UserInterface.shapeRenderer.begin(ShapeType.Line);
			Entry<Integer, WayPoint> ceilingEntry = waypointsCopy.ceilingEntry(entry.getKey() + 1);
			if (ceilingEntry != null) {
				float x2 = Fortress.worldToScreenX(ceilingEntry.getValue().waypoint.x);
				float y2 = Fortress.worldToScreenY(ceilingEntry.getValue().waypoint.y);
				UserInterface.shapeRenderer.line(x, y, x2, y2);
			}
			UserInterface.shapeRenderer.end();
		}
	}
	
	
	/**
	 * @return true if this {@link Path} contains no {@link WayPoint}s
	 */
	public boolean isEmpty() {
		return waypoints.isEmpty();
	}
	
	
	/**
	 * Adds a {@link WayPoint} at the specified position in the path, returning the {@link WayPoint} that has been replaced, if it existed.
	 */
	public synchronized WayPoint putWayPoint(int position, WayPoint wayPoint) {
		return waypoints.put(position, wayPoint);
	}
	
	
	/**
	 * @return the next {@link WayPoint} in this {@link Path}
	 */
	public synchronized WayPoint getNextPoint() {
		Entry<Integer, WayPoint> first = waypoints.firstEntry();
		return first == null ? null : first.getValue();
	}
	
	
	/**
	 * @return the WayPoint that was removed
	 */
	public synchronized WayPoint getAndRemoveNextWayPoint() {
		return waypoints.remove(waypoints.firstKey());
	}
	
	
	/** Returns the {@link TreeMap} containing the waypoints */
	public synchronized TreeMap<Integer, WayPoint> getWayPoints() {
		return waypoints;
	}
	
	
	/**
	 * WayPoint for {@link ArtificialIntelligence} to reach
	 *
	 * @author Matt
	 */
	public static class WayPoint implements Serializable {
		private static final long serialVersionUID = -8432865748395952201L;

		/**
		 * Constructor 
		 */
		public WayPoint(Vector2 waypoint, float tolerance) {
			this.waypoint = waypoint;
			this.tolerance = tolerance;
		}
		
		/**
		 * Constructor that sets {@link #tolerance} to 0
		 */
		public WayPoint(Vector2 waypoint) {
			this.waypoint = waypoint;
			this.tolerance = 0f;
		}
		
		/** The coordinate of the waypoint */
		public Vector2 waypoint;
		
		/** The tolerance distance of the waypoint */
		public float tolerance;
	}
}
