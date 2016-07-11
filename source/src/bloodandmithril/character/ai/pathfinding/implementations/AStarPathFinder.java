package bloodandmithril.character.ai.pathfinding.implementations;

import static bloodandmithril.util.Logger.aiDebug;
import static bloodandmithril.util.Logger.LogLevel.DEBUG;
import static bloodandmithril.util.Logger.LogLevel.INFO;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;
import static java.lang.Math.abs;
import static java.lang.Math.round;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.datastructure.DualKeyHashMap;
import bloodandmithril.util.datastructure.DualKeyHashMap.DualKeyEntry;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * An implementation of {@link PathFinder}, uses A* algorithm
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public final class AStarPathFinder extends PathFinder {
	private static final long serialVersionUID = 3045865381118362210L;

	/** Open {@link Node}s */
	private final DualKeyHashMap<Integer, Integer, Node> openNodes = new DualKeyHashMap<Integer, Integer, AStarPathFinder.Node>();

	/** Closed {@link Node}s */
	private final DualKeyHashMap<Integer, Integer, Node> closedNodes = new DualKeyHashMap<Integer, Integer, AStarPathFinder.Node>();

	/** Used for sorting {@link Node}s in ascending order by F = G + H */
	private static final Comparator<DualKeyEntry<Integer, Integer, Node>> fComparator = new Comparator<DualKeyEntry<Integer, Integer, Node>>() {
		@Override
		public int compare(final DualKeyEntry<Integer, Integer, Node> entry1, final DualKeyEntry<Integer, Integer, Node> entry2) {
			if (entry1.value.getF() > entry2.value.getF()) {
				return 1;
			} else if (entry1.value.getF() == entry2.value.getF()) {
				return 0;
			} else {
				return -1;
			}
		}
	};

	/** Used for sorting {@link Node}s in ascending order by H */
	private static final Comparator<DualKeyEntry<Integer, Integer, Node>> heuristicComparator = new Comparator<DualKeyEntry<Integer, Integer, Node>>() {
		@Override
		public int compare(final DualKeyEntry<Integer, Integer, Node> entry1, final DualKeyEntry<Integer, Integer, Node> entry2) {
			if (entry1.value.heuristic > entry2.value.heuristic) {
				return 1;
			} else if (entry1.value.heuristic == entry2.value.heuristic) {
				return 0;
			} else {
				return -1;
			}
		}
	};

	/**
	 * See {@link PathFinder#findShortestPathAir(WayPoint, WayPoint)}
	 */
	@Override
	public final Path findShortestPathAir(final WayPoint start, final WayPoint finish, final World world) {
		throw new UnsupportedOperationException();
	}


	/**
	 * See {@link PathFinder#findShortestPathGround(WayPoint, WayPoint, int)}
	 */
	@Override
	public final Path findShortestPathGround(final WayPoint start, final WayPoint finish, final int height, final int safeHeight, final float forceTolerance, final World world) {
		try {
			Vector2 startCoords = determineWayPointCoords(start.waypoint, false, false, world);
			Vector2 finishCoords = determineWayPointCoords(finish.waypoint, false, true, world);

			if (start.waypoint.y < 0) {
				startCoords = determineWayPointCoords(new Vector2(start.waypoint.x, start.waypoint.y + 1), false, false, world);
			}
			if (finish.waypoint.y < 0) {
				finishCoords = determineWayPointCoords(new Vector2(finish.waypoint.x, finish.waypoint.y + 1), false, true, world);
			}

			if (finishCoords == null) {
				// Destination is invalid, return an empty path
				return new Path();
			}

			final int startX = round(startCoords.x);
			final int startY = round(startCoords.y);

			final int finishX = round(finishCoords.x);
			final int finishY = round(finishCoords.y);

			final Node finishNode = new Node(finishX, finishY, null, null, null, safeHeight);
			aiDebug("Destination: " + finishNode.toString(), DEBUG);

			final Node startNode = new Node(startX, startY, null, null, finishNode, safeHeight);
			openNodes.put(startX, startY, startNode);

			boolean destinationFound = false;
			while(!destinationFound) {
				if (openNodes.getAllEntries().isEmpty()) {
					final List<DualKeyEntry<Integer, Integer, Node>> closedNodeEntries = closedNodes.getAllEntries();
					Collections.sort(closedNodeEntries, heuristicComparator);
					final DualKeyEntry<Integer, Integer, Node> closestEntry = closedNodeEntries.get(0);

					return extractPath(closestEntry.value, 0, forceTolerance, world);
				}

				final List<DualKeyEntry<Integer, Integer, Node>> allEntries = openNodes.getAllEntries();
				Collections.sort(allEntries, fComparator);
				final DualKeyEntry<Integer, Integer, Node> entry = allEntries.get(0);

				Node destination = null;

				try {
					destination = processOpenNodeGround(entry.value, finishNode, height, safeHeight, world);
				} catch (final UndiscoveredPathNotification e) {
					aiDebug("Detected undiscovered region", DEBUG);
				}

				if (destination != null) {
					aiDebug("Extracting path, begining from: " + destination.toString(), DEBUG);
					destinationFound = true;
					return extractPath(destination, finish.tolerance, forceTolerance, world);
				}
			}

			throw new RuntimeException("Failed to find path");
		} catch (final NoTileFoundException e) {
			aiDebug("NPE during pathfinding", LogLevel.DEBUG);
			return new Path();
		}
	}


	private final Vector2 determineWayPointCoords(final Vector2 location, final boolean floor, final boolean finish, final World world) throws NoTileFoundException {
		Tile tile = world.getTopography().getTile(location, true);
		if (location.y < 0) {
			tile = world.getTopography().getTile(location.x, location.y + 1, true);
		}

		if (tile.isPlatformTile && !(world.getTopography().getTile(location.x, location.y - TILE_SIZE, true) instanceof EmptyTile)) {
			return convertToWorldCoord(location, floor);
		} else {
			if (finish) {
				final Vector2 result = getGroundAboveOrBelowClosestEmptyOrPlatformSpace(location, 10, world);
				if (result != null) {
					return convertToWorldCoord(result, floor);
				} else {
					return null;
				}
			} else {
				return world.getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(location, floor);
			}
		}
	}


	/** Extracts the {@link Path} from the {@link #closedNodes} */
	private final Path extractPath(final Node finalNode, final float finalTolerance, final float forceTolerance, final World world) throws NoTileFoundException {
		final Path answer = new Path();

		if (finalNode.getF() > 9999999f) {
			final Node previousNode = closedNodes.get(finalNode.parentX, finalNode.parentY);
			final Vector2 previousVector = new Vector2(previousNode.x, previousNode.y);
			final float distance = Math.abs(previousVector.sub(new Vector2(finalNode.x, finalNode.y)).len());

			if (distance < forceTolerance) {
				return extractPath(previousNode, finalTolerance, forceTolerance, world);
			} else {
				return new Path();
			}
		}

		answer.addWayPointReversed(new WayPoint(determineWayPointCoords(new Vector2(finalNode.x, finalNode.y), true, false, world), finalTolerance));

		Node workingNode = finalNode;
		while(workingNode.parentX != null) {
			workingNode = closedNodes.get(workingNode.parentX, workingNode.parentY);

			if (workingNode.getF() > 9999999f) {
				return new Path();
			}

			aiDebug("Adding node to path: " + workingNode.toString(), DEBUG);
			answer.addWayPointReversed(new WayPoint(convertToWorldCoord(workingNode.x, workingNode.y, true), 0f));
		}

		final int size = answer.getSize();
		if (!answer.isEmpty() && size > 2) {
			answer.getAndRemoveNextWayPoint();
			answer.getNextPoint().tolerance = TILE_SIZE/2;
		}

		return answer;
	}


	/** Processes an open {@link Node} */
	private final Node processOpenNodeGround(final Node toProcess, final Node destinationNode, final int height, final int safeHeight, final World world) throws UndiscoveredPathNotification, NoTileFoundException {
		aiDebug("Processing open node: " + toProcess.toString(), DEBUG);
		openNodes.remove(toProcess.x, toProcess.y);
		closedNodes.put(toProcess.x, toProcess.y, toProcess);
		return processAdjascentNodesToOpenNodeGround(toProcess, destinationNode, height, safeHeight, world);
	}


	/** Processes the two adjascent locations to the argument */
	private final Node processAdjascentNodesToOpenNodeGround(final Node toProcess, final Node destinationNode, final int height, final int safeHeight, final World world) throws UndiscoveredPathNotification, NoTileFoundException {

		// Left and right nodes, the tiles immediately to the left/right of the open node to process
		final Node leftNode = new Node(toProcess.x - TILE_SIZE, toProcess.y, toProcess.x, toProcess.y, destinationNode, safeHeight);
		final Node rightNode = new Node(toProcess.x + TILE_SIZE, toProcess.y, toProcess.x, toProcess.y, destinationNode, safeHeight);


		if (isDestination(leftNode, destinationNode)) {
			return leftNode;
		}
		final int stepUpLeft = processAdjascent(leftNode, height, false, toProcess, destinationNode, safeHeight, world);
		final Node newLeftNode = new Node(leftNode.x, leftNode.y + stepUpLeft * TILE_SIZE, leftNode.parentX, leftNode.parentY, destinationNode, safeHeight);
		if (!isDestination(newLeftNode, destinationNode)) {
			if (stepUpLeft < 2) {
				addToOpenNodes(newLeftNode);
			}
		} else {
			aiDebug("Destination found: " + leftNode.toString(), DEBUG);
			return newLeftNode;
		}

		if (isDestination(rightNode, destinationNode)) {
			return rightNode;
		}
		final int stepUpRight = processAdjascent(rightNode, height, true, toProcess, destinationNode, safeHeight, world);
		final Node newRightNode = new Node(rightNode.x, rightNode.y + stepUpRight * TILE_SIZE, rightNode.parentX, rightNode.parentY, destinationNode, safeHeight);
		if (!isDestination(newRightNode, destinationNode)) {
			if (stepUpRight < 2) {
				addToOpenNodes(newRightNode);
			}
		} else {
			aiDebug("Destination found: " + rightNode.toString(), DEBUG);
			return newRightNode;
		}

		return null;
	}


	/** Checks if a {@link Node} contains the same coordinates and another */
	private final boolean isDestination(final Node isThisDestination, final Node destinationNode) {
		aiDebug("Checking if " + isThisDestination.toString() + " is the destination node: " + destinationNode.toString(), DEBUG);
		return isThisDestination.x.equals(destinationNode.x) && isThisDestination.y.equals(destinationNode.y);
	}


	/** Determines if we can move to an adjascent {@link Node} */
	@SuppressWarnings("cast")
	private final int processAdjascent(final Node to, final int height, final boolean right, final Node parent, final Node destination, final int safeHeight, final World world) throws UndiscoveredPathNotification, NoTileFoundException {
		Tile tile;

		try {
			tile = world.getTopography().getTile((float)to.x, (float)to.y, true);

			// If the tile is empty, we check if all tiles above it (within specified height) are also empty, if not, we return 2, otherwise, we return the vertical distance
			// To the empty tile that is above the non-empty tile below the adjascent tile
			if (tile instanceof EmptyTile) {
				for (int i = 1; i <= height; i++) {
					if (!world.getTopography().getTile(to.x, (float)to.y + i * TILE_SIZE, true).isPassable()) {
						return 2;
					}
				}
				cascadeDownAndProcessPlatforms(to.x, to.y - TILE_SIZE, parent, destination, height, safeHeight, world);
				return -round(to.y - world.getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(to.x, to.y, false).y) / TILE_SIZE;

			// If the tile is not empty and not a platform, we check that all tiles above it (within specified height) are also empty, if so, we return 1, otherwise we return 2.
			} else if (!tile.isPlatformTile) {
				// Note we're using height + 1 because we're stepping up
				for (int i = 1; i <= height + 1; i++) {
					if (!world.getTopography().getTile(to.x, (float)to.y + i * TILE_SIZE, true).isPassable()) {
						return 2;
					}
				}
				if (!world.getTopography().getTile((float)to.x + (float)(right ? -TILE_SIZE : TILE_SIZE), to.y + (height + 1) * TILE_SIZE, true).isPassable()) {
					return 2;
				}
				return 1;

			// The tile is a platform. Cascade downward and add all platforms to open nodes, until we hit a non-platform, the return value
			// will be used for the top most node, added one up the call stack
			} else {
				cascadeDownAndProcessPlatforms(to.x, to.y - TILE_SIZE, parent, destination, height, safeHeight, world);

				for (int i = 1; i <= height + 1; i++) {
					if (!world.getTopography().getTile(to.x, (float)to.y + i * TILE_SIZE, true).isPassable()) {
						return 2;
					}
				}

				if (!world.getTopography().getTile((float)to.x + (float)(right ? -TILE_SIZE : TILE_SIZE), to.y + (height + 1) * TILE_SIZE, true).isPassable()) {
					return 2;
				}
				return 1;
			}
		} catch (final NoTileFoundException e) {
			throw new UndiscoveredPathNotification();
		}
	}


	/** Cascades downwards and adds all platform tiles to {@link #openNodes} until a non-platform, non-empty {@link Tile} is found */
	private final void cascadeDownAndProcessPlatforms(final float x, final float y, final Node parent, final Node destination, final int height, final int safeHeight, final World world) throws NoTileFoundException {
		Tile tile;
		try {
			tile = world.getTopography().getTile(x, y, true);
		} catch (final NoTileFoundException e) {
			aiDebug("Null tile encountered, perhaps not yet generated", INFO);
			return;
		}

		// If we're on empty, recursively call self with lower coordinate.
		if (tile instanceof EmptyTile) {
			cascadeDownAndProcessPlatforms(x, y - TILE_SIZE, parent, destination, height, safeHeight, world);

		} else {
			// If we're not empty, check for legality, add the one above to open
			// nodes.
			boolean legal = true;
			for (int i = 1; i <= height + 1; i++) {
				if (!world.getTopography().getTile(x, y + i * TILE_SIZE, true).isPassable()) {
					legal = false;
					break;
				}
			}

			if (legal) {
				final Node newNode = new Node(round(x), round(y + TILE_SIZE), parent.x, parent.y, destination, safeHeight);
				addToOpenNodes(newNode);
			}

			// If this non-empty is a platform, recursively call self with lower
			// coordinate.
			if (tile.isPlatformTile) {
				cascadeDownAndProcessPlatforms(x, y - TILE_SIZE, parent, destination, height, safeHeight, world);
			}

			// If this non-empty is not a platform, stop.
		}
	}


	/** Heuristic calculation */
	private final float calculateHeuristic(final int x, final int y, final Vector2 destination) {
		return abs(destination.cpy().sub(new Vector2(x, y)).len());
	}


	/** Adds a {@link Node} to {@link #openNodes} */
	private final void addToOpenNodes(final Node nodeToPut) {
		if (closedNodes.get(nodeToPut.x, nodeToPut.y) == null) {
			if (openNodes.get(nodeToPut.x, nodeToPut.y) == null) {
				openNodes.put(nodeToPut.x, nodeToPut.y, nodeToPut);
			} else {
				if (openNodes.get(nodeToPut.x, nodeToPut.y).getF() > nodeToPut.getF()) {
					openNodes.put(nodeToPut.x, nodeToPut.y, nodeToPut);
				}
			}
		} else {
			if (closedNodes.get(nodeToPut.x, nodeToPut.y).getF() > nodeToPut.getF()) {
				openNodes.put(nodeToPut.x, nodeToPut.y, nodeToPut);
			}
		}
	}


	/**
	 * A Node used as part of the A* algorithm
	 *
	 * @author Matt
	 */
	public final class Node implements Serializable {
		private static final long serialVersionUID = 3817098095133274007L;

		/** {@link Node} coordinates */
		private final Integer x, y, parentX, parentY;

		/** The heuristic, cost or 'H' and 'G' of the A* equation */
		private float heuristic, cost;

		/**
		 * Constructor
		 */
		private Node(final Integer x, final Integer y, final Integer parentX, final Integer parentY, final Node destination, final int safeHeight) {
			this.x = x;
			this.y = y;
			this.parentX = parentX;
			this.parentY = parentY;

			if (destination != null) {
				this.heuristic = calculateHeuristic(x, y, new Vector2(destination.x, destination.y));
			}

			if (parentX != null && parentY != null) {
				final float distance = abs(new Vector2(x, y).sub(parentX, parentY).len());
				this.cost = distance > safeHeight ? Float.MAX_VALUE : distance + closedNodes.get(parentX, parentY).cost;
			}
		}

		private final float getF() {
			return heuristic + cost;
		}

		@Override
		public String toString() {
			return "(" + x + ", " + y + ") Parent (" + parentX + ", " + parentY + ") " + "\n" +
				   "Cost: " + cost + ", Heuristic: " + heuristic + ", F: " + getF();
		}
	}


	/**
	 * Dummy class
	 *
	 * @author Matt
	 */
	private final class UndiscoveredPathNotification extends Throwable {
		private static final long serialVersionUID = -7186752075109145987L;
	}
}