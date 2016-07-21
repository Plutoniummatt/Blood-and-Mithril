package bloodandmithril.character.ai.pathfinding;

import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Performance;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;


/**
 * A Path for {@link ArtificialIntelligence} to follow
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public final class Path implements Serializable {
	private static final long serialVersionUID = -2569430046328226956L;

	/** {@link WayPoint}s associated with this {@link Path} */
	private final ConcurrentLinkedDeque<WayPoint> waypoints;

	/**
	 * True if location is part of this {@link Path}
	 */
	public synchronized final boolean isPartOfPathGroundAndIsNext(final Vector2 location) {
		try {
			final Vector2 flooredCoords = Topography.convertToWorldCoord(location, true);
			if (!waypoints.isEmpty() && waypoints.getFirst().waypoint.equals(flooredCoords)) {
				return true;
			}
		} catch (final NoTileFoundException e) {
			return false;
		}
		return false;
	}


	/**
	 * True if location is directly above the next waypoint in the {@link Path}
	 */
	public synchronized final boolean isDirectlyAboveNext(final Vector2 location) {
		try {
			Vector2 flooredCoords = Topography.convertToWorldCoord(location, true);
			if (location.y < 0) {
				flooredCoords = Topography.convertToWorldCoord(location.x, location.y + 1, true);
			}
			if (!waypoints.isEmpty() &&
					waypoints.getFirst().waypoint.x == flooredCoords.x &&
					waypoints.getFirst().waypoint.y < flooredCoords.y) {
				return true;
			}
		} catch (final NoTileFoundException e) {
			return false;
		}
		return false;
	}


	/** Public no-arg constructor */
	public Path() {
		 this.waypoints = new ConcurrentLinkedDeque<WayPoint>();
	}


	/** Private constructor, see {@link #copy()} */
	private Path(final ConcurrentLinkedDeque<WayPoint> waypoints){
		this.waypoints = waypoints;
	}


	/** Copies this path, must be synchronized */
	public synchronized final Path copy() {
		return new Path(new ConcurrentLinkedDeque<>(this.waypoints));
	}


	/** Adds a {@link WayPoint} to this {@link Path} at the beginning */
	public synchronized final void addWayPointReversed(final WayPoint wayPoint) {
		waypoints.addFirst(wayPoint);
	}


	/** Clears all {@link WayPoint}s of this {@link Path} */
	public synchronized final void clear() {
		waypoints.clear();
	}


	/**
	 * Renders all the waypoint of this {@link Path}
	 */
	@Performance(explanation = "Renders a dot for each waypoint, inefficient if path contains many waypoints")
	public final void render() {
		final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
		final LinkedList<WayPoint> waypointsCopy = Lists.newLinkedList(waypoints);
		final Iterator<WayPoint> waypointsIterator = waypointsCopy.iterator();

		WayPoint current;
		if (waypointsIterator.hasNext()) {
			current = waypointsIterator.next();
		} else {
			return;
		}

		float x = worldToScreenX(current.waypoint.x);
		float y = worldToScreenY(current.waypoint.y);

		userInterface.getShapeRenderer().begin(ShapeType.Filled);
		userInterface.getShapeRenderer().circle(x, y, 3);
		userInterface.getShapeRenderer().end();

		do {
			x = worldToScreenX(current.waypoint.x);
			y = worldToScreenY(current.waypoint.y);

			if (waypointsIterator.hasNext()) {
				current = waypointsIterator.next();

				final float x2 = worldToScreenX(current.waypoint.x);
				final float y2 = worldToScreenY(current.waypoint.y);

				userInterface.getShapeRenderer().begin(ShapeType.Line);
				userInterface.getShapeRenderer().line(x, y, x2, y2);
				userInterface.getShapeRenderer().end();

				userInterface.getShapeRenderer().begin(ShapeType.Filled);
				userInterface.getShapeRenderer().circle(x2, y2, 3);
				userInterface.getShapeRenderer().end();
			}
		} while (waypointsIterator.hasNext());
	}


	/**
	 * @return true if this {@link Path} contains no {@link WayPoint}s
	 */
	public final boolean isEmpty() {
		return waypoints.isEmpty();
	}


	/**
	 * @return the next {@link WayPoint} in this {@link Path}
	 */
	public synchronized final WayPoint getNextPoint() {
		if (waypoints.isEmpty()) {
			return null;
		}

		return waypoints.getFirst();
	}


	/**
	 * @return the WayPoint that was removed
	 */
	public synchronized final WayPoint getAndRemoveNextWayPoint() {
		return waypoints.remove();
	}


	/** Returns the destination waypoint */
	public synchronized final WayPoint getDestinationWayPoint() {
		if (waypoints.isEmpty()) {
			return null;
		}
		return waypoints.getLast();
	}


	public int getSize() {
		return waypoints.size();
	}


	public synchronized ConcurrentLinkedDeque<WayPoint> getWayPoints() {
		return waypoints;
	}


	/**
	 * WayPoint for {@link ArtificialIntelligence} to reach
	 *
	 * @author Matt
	 */
	public static final class WayPoint implements Serializable {
		private static final long serialVersionUID = -8432865748395952201L;

		/** The coordinate of the waypoint */
		public final Vector2 waypoint;

		/** The tolerance distance of the waypoint */
		public float tolerance;

		/**
		 * Constructor
		 */
		public WayPoint(final Vector2 waypoint, final float tolerance) {
			this.waypoint = waypoint;
			this.tolerance = tolerance;
		}

		/**
		 * Constructor that sets {@link #tolerance} to 0
		 */
		public WayPoint(final Vector2 waypoint) {
			this.waypoint = waypoint;
			this.tolerance = 0f;
		}
	}
}
