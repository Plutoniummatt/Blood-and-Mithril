package bloodandmithril.character.ai.pathfinding;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;


/**
 * A Path for {@link ArtificialIntelligence} to follow
 *
 * @author Matt
 */
public class Path implements Serializable {
	private static final long serialVersionUID = -2569430046328226956L;

	/** {@link WayPoint}s associated with this {@link Path} */
	private ConcurrentLinkedDeque<WayPoint> waypoints;

	/**
	 * True if location is part of this {@link Path}
	 */
	public synchronized boolean isPartOfPathGroundAndIsNext(Vector2 location) {
		Vector2 flooredCoords = Topography.convertToWorldCoord(location, true);
		if (!waypoints.isEmpty() && waypoints.getFirst().waypoint.equals(flooredCoords)) {
			return true;
		}
		return false;
	}


	/**
	 * True if location is directly above the next waypoint in the {@link Path}
	 */
	public synchronized boolean isDirectlyAboveNext(Vector2 location) {
		Vector2 flooredCoords = Topography.convertToWorldCoord(location, true);
		if (location.y < 0) {
			flooredCoords = Topography.convertToWorldCoord(location.x, location.y + 1, true);
		}
		if (!waypoints.isEmpty() &&
			waypoints.getFirst().waypoint.x == flooredCoords.x &&
			waypoints.getFirst().waypoint.y < flooredCoords.y) {
			return true;
		}
		return false;
	}
	
	
	/** Public no-arg constructor */
	public Path() {
		 this.waypoints = new ConcurrentLinkedDeque<WayPoint>();
	}
	
	
	/** Private constructor, see {@link #copy()} */
	private Path(ConcurrentLinkedDeque<WayPoint> waypoints){
		this.waypoints = waypoints;
	}
	
	
	/** Copies this path, must be synchronized */
	public synchronized Path copy() {
		return new Path(new ConcurrentLinkedDeque<>(this.waypoints));
	}

	
	/** Adds a {@link WayPoint} to this {@link Path} at the beginning */
	public synchronized void addWayPointReversed(WayPoint wayPoint) {
		waypoints.addFirst(wayPoint);
	}


	/** Clears all {@link WayPoint}s of this {@link Path} */
	public synchronized void clear() {
		waypoints.clear();
	}


	/**
	 * Renders all the waypoint of this {@link Path}
	 */
	public void render() {
		LinkedList<WayPoint> waypointsCopy = Lists.newLinkedList(waypoints);
		Iterator<WayPoint> waypointsIterator = waypointsCopy.iterator();

		WayPoint current;
		if (waypointsIterator.hasNext()) {
			current = waypointsIterator.next();
		} else {
			return;
		}

		float x = BloodAndMithrilClient.worldToScreenX(current.waypoint.x);
		float y = BloodAndMithrilClient.worldToScreenY(current.waypoint.y);

		UserInterface.shapeRenderer.begin(ShapeType.FilledCircle);
		UserInterface.shapeRenderer.filledCircle(x, y, 3);
		UserInterface.shapeRenderer.end();

		do {
			x = BloodAndMithrilClient.worldToScreenX(current.waypoint.x);
			y = BloodAndMithrilClient.worldToScreenY(current.waypoint.y);

			if (waypointsIterator.hasNext()) {
				current = waypointsIterator.next();

				float x2 = BloodAndMithrilClient.worldToScreenX(current.waypoint.x);
				float y2 = BloodAndMithrilClient.worldToScreenY(current.waypoint.y);

				UserInterface.shapeRenderer.begin(ShapeType.Line);
				UserInterface.shapeRenderer.line(x, y, x2, y2);
				UserInterface.shapeRenderer.end();

				UserInterface.shapeRenderer.begin(ShapeType.FilledCircle);
				UserInterface.shapeRenderer.filledCircle(x2, y2, 3);
				UserInterface.shapeRenderer.end();
			}
		} while (waypointsIterator.hasNext());
	}


	/**
	 * @return true if this {@link Path} contains no {@link WayPoint}s
	 */
	public boolean isEmpty() {
		return waypoints.isEmpty();
	}


	/**
	 * @return the next {@link WayPoint} in this {@link Path}
	 */
	public synchronized WayPoint getNextPoint() {
		if (waypoints.isEmpty()) {
			return null;
		}

		return waypoints.getFirst();
	}


	/**
	 * @return the WayPoint that was removed
	 */
	public synchronized WayPoint getAndRemoveNextWayPoint() {
		return waypoints.remove();
	}


	/** Returns the destination waypoint */
	public WayPoint getDestinationWayPoint() {
		if (waypoints.isEmpty()) {
			return null;
		}
		return waypoints.getLast();
	}


	public int getSize() {
		return waypoints.size();
	}


	/**
	 * WayPoint for {@link ArtificialIntelligence} to reach
	 *
	 * @author Matt
	 */
	public static class WayPoint implements Serializable {
		private static final long serialVersionUID = -8432865748395952201L;

		/** The coordinate of the waypoint */
		public Vector2 waypoint;

		/** The tolerance distance of the waypoint */
		public float tolerance;

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
	}
}
