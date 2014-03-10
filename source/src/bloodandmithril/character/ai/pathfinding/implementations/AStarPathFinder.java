package bloodandmithril.character.ai.pathfinding.implementations;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;
import static java.lang.Math.abs;
import static java.lang.Math.round;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.DualKeyHashMap;
import bloodandmithril.util.datastructure.DualKeyHashMap.DualKeyEntry;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.DebugTile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.badlogic.gdx.math.Vector2;

/**
 * An implementation of {@link PathFinder}, uses A* algorithm
 *
 * @author Matt
 */
public class AStarPathFinder extends PathFinder {
	private static final long serialVersionUID = 3045865381118362210L;

	/** Open {@link Node}s */
	private final DualKeyHashMap<Integer, Integer, Node> openNodes = new DualKeyHashMap<Integer, Integer, AStarPathFinder.Node>();

	/** Closed {@link Node}s */
	private final DualKeyHashMap<Integer, Integer, Node> closedNodes = new DualKeyHashMap<Integer, Integer, AStarPathFinder.Node>();

	/** Used for sorting {@link Node}s in ascending order by F = G + H */
	private static Comparator<DualKeyEntry<Integer, Integer, Node>> fComparator = new Comparator<DualKeyEntry<Integer, Integer, Node>>() {
		@Override
		public int compare(DualKeyEntry<Integer, Integer, Node> entry1, DualKeyEntry<Integer, Integer, Node> entry2) {
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
	private static Comparator<DualKeyEntry<Integer, Integer, Node>> heuristicComparator = new Comparator<DualKeyEntry<Integer, Integer, Node>>() {
		@Override
		public int compare(DualKeyEntry<Integer, Integer, Node> entry1, DualKeyEntry<Integer, Integer, Node> entry2) {
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
	public Path findShortestPathAir(WayPoint start, WayPoint finish, World world) {
		return null;
	}


	/**
	 * See {@link PathFinder#findShortestPathGround(WayPoint, WayPoint, int)}
	 */
	@Override
	public Path findShortestPathGround(WayPoint start, WayPoint finish, int height, int safeHeight, float forceTolerance, World world) {
		Vector2 startCoords = determineWayPointCoords(start.waypoint, false, false, world);
		Vector2 finishCoords = determineWayPointCoords(finish.waypoint, false, true, world);

		if (start.waypoint.y < 0) {
			startCoords = determineWayPointCoords(new Vector2(start.waypoint.x, start.waypoint.y + 1), false, false, world);
		}
		if (finish.waypoint.y < 0) {
			finishCoords = determineWayPointCoords(new Vector2(finish.waypoint.x, finish.waypoint.y + 1), false, true, world);
		}

		if (finishCoords == null) {
			return new Path();
		}

		int startX = round(startCoords.x);
		int startY = round(startCoords.y);

		int finishX = round(finishCoords.x);
		int finishY = round(finishCoords.y);

		Node finishNode = new Node(finishX, finishY, null, null, null, safeHeight);
		Logger.aiDebug("Destination: " + finishNode.toString(), LogLevel.DEBUG);

		Node startNode = new Node(startX, startY, null, null, finishNode, safeHeight);
		openNodes.put(startX, startY, startNode);

		boolean destinationFound = false;
		while(!destinationFound) {
			if (openNodes.getAllEntries().isEmpty()) {
				List<DualKeyEntry<Integer, Integer, Node>> closedNodeEntries = closedNodes.getAllEntries();
				Collections.sort(closedNodeEntries, heuristicComparator);
				DualKeyEntry<Integer, Integer, Node> closestEntry = closedNodeEntries.get(0);

				return extractPath(closestEntry.value, 0, forceTolerance, world);
			}

			List<DualKeyEntry<Integer, Integer, Node>> allEntries = openNodes.getAllEntries();
			Collections.sort(allEntries, fComparator);
			DualKeyEntry<Integer, Integer, Node> entry = allEntries.get(0);

			if (entry.value.getF() > 9999999f) {
				return new Path();
			}

			Node destination = null;

			try {
				destination = processOpenNodeGround(entry.value, finishNode, height, safeHeight, world);
			} catch (UndiscoveredPathNotification e) {
				Logger.aiDebug("Detected undiscovered region", LogLevel.DEBUG);
			}

			if (destination != null) {
				Logger.aiDebug("Extracting path, begining from: " + destination.toString(), LogLevel.DEBUG);
				destinationFound = true;
				return extractPath(destination, finish.tolerance, forceTolerance, world);
			}
		}

		throw new RuntimeException("Failed to find path");
	}


	private Vector2 determineWayPointCoords(Vector2 location, boolean floor, boolean finish, World world) {
		Tile tile = world.getTopography().getTile(location, true);
		if (location.y < 0) {
			tile = world.getTopography().getTile(location.x, location.y + 1, true);
		}

		if (tile.isPlatformTile && !(world.getTopography().getTile(location.x, location.y - TILE_SIZE, true) instanceof EmptyTile)) {
			return convertToWorldCoord(location, floor);
		} else {
			if (finish) {
				Vector2 result = getGroundAboveOrBelowClosestEmptyOrPlatformSpace(location, 10, world);
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
	private Path extractPath(Node finalNode, float finalTolerance, float forceTolerance, World world) {
		Path answer = new Path();

		if (finalNode.getF() > 9999999f) {
			Node previousNode = closedNodes.get(finalNode.parentX, finalNode.parentY);
			Vector2 previousVector = new Vector2(previousNode.x, previousNode.y);
			float distance = Math.abs(previousVector.sub(new Vector2(finalNode.x, finalNode.y)).len());

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

			Logger.aiDebug("Adding node to path: " + workingNode.toString(), LogLevel.DEBUG);
			answer.addWayPointReversed(new WayPoint(convertToWorldCoord(workingNode.x, workingNode.y, true), 0f));
		}

		int size = answer.getSize();
		if (!answer.isEmpty() && size > 2) {
			answer.getAndRemoveNextWayPoint();
			answer.getNextPoint().tolerance = TILE_SIZE/2;
		}

		return answer;
	}


	/** Processes an open {@link Node} */
	private Node processOpenNodeGround(Node toProcess, Node destinationNode, int height, int safeHeight, World world) throws UndiscoveredPathNotification {
		Logger.aiDebug("Processing open node: " + toProcess.toString(), LogLevel.DEBUG);
		openNodes.remove(toProcess.x, toProcess.y);
		closedNodes.put(toProcess.x, toProcess.y, toProcess);
		return processAdjascentNodesToOpenNodeGround(toProcess, destinationNode, height, safeHeight, world);
	}


	/** Processes the two adjascent locations to the argument */
	private Node processAdjascentNodesToOpenNodeGround(Node toProcess, Node destinationNode, int height, int safeHeight, World world) throws UndiscoveredPathNotification {

		// Left and right nodes, the tiles immediately to the left/right of the open node to process
		Node leftNode = new Node(toProcess.x - Topography.TILE_SIZE, toProcess.y, toProcess.x, toProcess.y, destinationNode, safeHeight);
		Node rightNode = new Node(toProcess.x + Topography.TILE_SIZE, toProcess.y, toProcess.x, toProcess.y, destinationNode, safeHeight);


		if (isDestination(leftNode, destinationNode)) {
			return leftNode;
		}
		int stepUpLeft = processAdjascent(leftNode, height, false, toProcess, destinationNode, safeHeight, world);
		Node newLeftNode = new Node(leftNode.x, leftNode.y + stepUpLeft * Topography.TILE_SIZE, leftNode.parentX, leftNode.parentY, destinationNode, safeHeight);
		if (!isDestination(newLeftNode, destinationNode)) {
			if (stepUpLeft < 2) {
				addToOpenNodes(newLeftNode);
			}
		} else {
			Logger.aiDebug("Destination found: " + leftNode.toString(), LogLevel.DEBUG);
			return newLeftNode;
		}

		if (isDestination(rightNode, destinationNode)) {
			return rightNode;
		}
		int stepUpRight = processAdjascent(rightNode, height, true, toProcess, destinationNode, safeHeight, world);
		Node newRightNode = new Node(rightNode.x, rightNode.y + stepUpRight * Topography.TILE_SIZE, rightNode.parentX, rightNode.parentY, destinationNode, safeHeight);
		if (!isDestination(newRightNode, destinationNode)) {
			if (stepUpRight < 2) {
				addToOpenNodes(newRightNode);
			}
		} else {
			Logger.aiDebug("Destination found: " + rightNode.toString(), LogLevel.DEBUG);
			return newRightNode;
		}

		return null;
	}


	/** Checks if a {@link Node} contains the same coordinates and another */
	private boolean isDestination(final Node isThisDestination, final Node destinationNode) {
		Logger.aiDebug("Checking if " + isThisDestination.toString() + " is the destination node: " + destinationNode.toString(), LogLevel.DEBUG);
		return isThisDestination.x.equals(destinationNode.x) && isThisDestination.y.equals(destinationNode.y);
	}


	/** Determines if we can move to an adjascent {@link Node} */
	private int processAdjascent(final Node to, int height, boolean right, Node parent, Node destination, int safeHeight, World world) throws UndiscoveredPathNotification {
		Tile tile;

		try {
			tile = world.getTopography().getTile(to.x, to.y, true);

			// If the tile is empty, we check if all tiles above it (within specified height) are also empty, if not, we return 2, otherwise, we return the vertical distance
			// To the empty tile that is above the non-empty tile below the adjascent tile
			if (tile instanceof EmptyTile) {
				changeToDebugTile(to.x, to.y, world);
				for (int i = 1; i <= height; i++) {
					changeToDebugTile(to.x, to.y + i * TILE_SIZE, world);
					if (!world.getTopography().getTile(to.x, to.y + i * TILE_SIZE, true).isPassable()) {
						return 2;
					}
				}
				cascadeDownAndProcessPlatforms(to.x, to.y - TILE_SIZE, parent, destination, height, safeHeight, world);
				changeToDebugTile(to.x, world.getTopography().getLowestEmptyOrPlatformTileWorldCoords(to.x, to.y, false).y, world);
				return -round(to.y - world.getTopography().getLowestEmptyOrPlatformTileWorldCoords(to.x, to.y, false).y) / Topography.TILE_SIZE;

			// If the tile is not empty and not a platform, we check that all tiles above it (within specified height) are also empty, if so, we return 1, otherwise we return 2.
			} else if (!tile.isPlatformTile) {
				// Note we're using height + 1 because we're stepping up
				for (int i = 1; i <= height + 1; i++) {
					changeToDebugTile(to.x, to.y + i * TILE_SIZE, world);
					if (!world.getTopography().getTile(to.x, to.y + i * TILE_SIZE, true).isPassable()) {
						return 2;
					}
				}
				changeToDebugTile(to.x + (right ? -TILE_SIZE : TILE_SIZE), to.y + (height + 1) * TILE_SIZE, world);
				if (!world.getTopography().getTile(to.x + (right ? -TILE_SIZE : TILE_SIZE), to.y + (height + 1) * TILE_SIZE, true).isPassable()) {
					return 2;
				}
				return 1;

			// The tile is a platform. Cascade downward and add all platforms to open nodes, until we hit a non-platform, the return value
			// will be used for the top most node, added one up the call stack
			} else {
				cascadeDownAndProcessPlatforms(to.x, to.y - TILE_SIZE, parent, destination, height, safeHeight, world);

				for (int i = 1; i <= height + 1; i++) {
					changeToDebugTile(to.x, to.y + i * TILE_SIZE, world);
					if (!world.getTopography().getTile(to.x, to.y + i * TILE_SIZE, true).isPassable()) {
						return 2;
					}
				}

				changeToDebugTile(to.x + (right ? -TILE_SIZE : TILE_SIZE), to.y + (height + 1) * TILE_SIZE, world);
				if (!world.getTopography().getTile(to.x + (right ? -TILE_SIZE : TILE_SIZE), to.y + (height + 1) * TILE_SIZE, true).isPassable()) {
					return 2;
				}
				return 1;
			}
		} catch (RuntimeException e) {
			throw new UndiscoveredPathNotification();
		}
	}


	/** Cascades downwards and adds all platform tiles to {@link #openNodes} until a non-platform, non-empty {@link Tile} is found */
	private void cascadeDownAndProcessPlatforms(float x, float y, Node parent, Node destination, int height, int safeHeight, World world) {
		Tile tile;
		try {
			tile = world.getTopography().getTile(x, y, true);
			changeToDebugTile(x, y, world);
		} catch (NullPointerException e) {
			Logger.aiDebug("Null tile encountered, perhaps not yet generated", LogLevel.INFO);
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
				changeToDebugTile(x, y + i * TILE_SIZE, world);
				if (!world.getTopography().getTile(x, y + i * TILE_SIZE, true).isPassable()) {
					legal = false;
					break;
				}
			}

			if (legal) {
				Node newNode = new Node(round(x), round(y + TILE_SIZE), parent.x, parent.y, destination, safeHeight);
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


	private void changeToDebugTile(final float x, final float y, final World world) {
		if (ClientServerInterface.isServer() || ClientServerInterface.isClient()) {
			return;
		}

		Topography.addTask(new Task() {
			@Override
			public void execute() {
				world.getTopography().changeTile(x, y, false, DebugTile.class);
			}
		});
	}


	/** Heuristic calculation */
	private float calculateHeuristic(int x, int y, Vector2 destination) {
		return abs(destination.cpy().sub(new Vector2(x, y)).len());
	}


	/** Adds a {@link Node} to {@link #openNodes} */
	private void addToOpenNodes(Node nodeToPut) {
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
	public class Node implements Serializable {
		private static final long serialVersionUID = 3817098095133274007L;

		/** {@link Node} coordinates */
		private final Integer x, y, parentX, parentY;

		/** The heuristic, cost or 'H' and 'G' of the A* equation */
		private float heuristic, cost;

		/**
		 * Constructor
		 */
		private Node(Integer x, Integer y, Integer parentX, Integer parentY, Node destination, int safeHeight) {
			this.x = x;
			this.y = y;
			this.parentX = parentX;
			this.parentY = parentY;

			if (destination != null) {
				this.heuristic = calculateHeuristic(x, y, new Vector2(destination.x, destination.y));
			}

			if (parentX != null && parentY != null) {
				float distance = abs(new Vector2(x, y).sub(parentX, parentY).len());
				this.cost = distance > safeHeight ? 10000000f : distance + closedNodes.get(parentX, parentY).cost;
			}
		}

		private float getF() {
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
	private class UndiscoveredPathNotification extends Throwable {
		private static final long serialVersionUID = -7186752075109145987L;
	}
}