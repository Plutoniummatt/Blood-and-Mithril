package bloodandmithril.character.ai.task;

import static bloodandmithril.core.BloodAndMithrilClient.getKeyMappings;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.pathfinding.implementations.AStarPathFinder;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Performance;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

/**
 * An {@link AITask} that moves an {@link Individual} to a location through the use of a {@link PathFinder}.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class GoToLocation extends AITask {
	private static final long serialVersionUID = -4121947217713585991L;

	/** The {@link Path} this {@link GoToLocation} {@link AITask} will follow */
	private Path path;

	/** Whether or not to fly */
	private final boolean fly;

	/** Used to calculate whether the {@link #host} is stuck */
	private int stuckCounter = 0;

	/** Optional termination function */
	private SerializableFunction<Boolean> function;

	/**
	 * Constructor
	 */
	private GoToLocation(Individual host, Vector2 start, WayPoint destination, boolean fly, float forceTolerance, boolean safe) {
		super(host.getId());
		this.fly = fly;

		int blockspan = host.getHeight()/Topography.TILE_SIZE + (host.getHeight() % Topography.TILE_SIZE == 0 ? 0 : 1) - 1;

		PathFinder pathFinder = new AStarPathFinder();

		this.path = fly ?
			pathFinder.findShortestPathAir(new WayPoint(start), destination, Domain.getWorld(host.getWorldId())):
			pathFinder.findShortestPathGround(new WayPoint(start), destination, blockspan, safe ? host.getSafetyHeight() : 1000, forceTolerance, Domain.getWorld(host.getWorldId()));
	}


	/**
	 * Constructor
	 */
	private GoToLocation(Individual host, Vector2 start, WayPoint destination, boolean fly, SerializableFunction<Boolean> function, boolean safe) {
		super(host.getId());
		this.fly = fly;
		this.function = function;

		int blockspan = host.getHeight()/Topography.TILE_SIZE + (host.getHeight() % Topography.TILE_SIZE == 0 ? 0 : 1) - 1;

		PathFinder pathFinder = new AStarPathFinder();

		this.path = fly ?
			pathFinder.findShortestPathAir(new WayPoint(start), destination, Domain.getWorld(host.getWorldId())):
			pathFinder.findShortestPathGround(new WayPoint(start), destination, blockspan, safe ? host.getSafetyHeight() : 1000, 150f, Domain.getWorld(host.getWorldId()));
	}


	public static GoToLocation goTo(Individual host, Vector2 start, WayPoint destination, boolean fly, float forceTolerance, boolean safe) {
		return new GoToLocation(host, start, destination, fly, forceTolerance, safe);
	}


	public static GoToLocation goToWithTerminationFunction(Individual host, Vector2 start, WayPoint destination, boolean fly, SerializableFunction<Boolean> function, boolean safe) {
		return new GoToLocation(host, start, destination, fly, function, safe);
	}


	/**
	 * True if the {@link #path} contains a {@link WayPoint} representing the {@link Tile} at this location
	 */
	public boolean isPartOfPath(Vector2 location) {
		return path.isPartOfPathGroundAndIsNext(location);
	}


	/**
	 * See {@link Path#isDirectlyAboveNext(Vector2)}
	 */
	public boolean isAboveNext(Vector2 location) {
		return path.isDirectlyAboveNext(location);
	}


	@Override
	public void execute(float delta) {
		if (!path.isEmpty()) {
			if (fly) {
				// TODO Flying
			} else {
				Vector2 waypoint = path.getNextPoint().waypoint;
				try {
					if (Domain.getWorld(getHost().getWorldId()).getTopography().getTile(waypoint.x, waypoint.y - Topography.TILE_SIZE / 2, true).isPassable() &&
						Domain.getWorld(getHost().getWorldId()).getTopography().getTile(waypoint.x, waypoint.y - 3 * Topography.TILE_SIZE / 2, true).isPassable() &&
						!Domain.getWorld(getHost().getWorldId()).getTopography().getTile(waypoint.x, waypoint.y - Topography.TILE_SIZE / 2, true).isPlatformTile) {

						getHost().speak("Looks like I'm stuck...", 1500);
						path.clear();
					} else {
						goToWayPoint(path.getNextPoint(), 4);
					}
				} catch (NoTileFoundException e) {}
			}
		}
	}


	@Override
	public String getDescription() {
		return "Moving";
	}


	/**
	 * Renders the {@link #path} as well as the current trajectory
	 *
	 * Use with care, due to the inefficiency of this method, it is deprecated, intended for debugging purposes only
	 */
	@Performance(explanation = "This method draws a dot and a line between each waypoint, rather inefficient if the path is long")
	public void renderPath() {
		WayPoint nextPoint = path.getNextPoint();

		if (nextPoint != null && nextPoint.waypoint != null) {
			UserInterface.shapeRenderer.begin(ShapeType.Line);
			float startX = Domain.getIndividual(hostId.getId()).getState().position.x;
			float startY = Domain.getIndividual(hostId.getId()).getState().position.y;
			float endX = nextPoint.waypoint.x;
			float endY = nextPoint.waypoint.y;

			UserInterface.shapeRenderer.line(
				BloodAndMithrilClient.worldToScreenX(startX),
				BloodAndMithrilClient.worldToScreenY(startY),
				BloodAndMithrilClient.worldToScreenX(endX),
				BloodAndMithrilClient.worldToScreenY(endY)
			);

			UserInterface.shapeRenderer.end();

			path.render();
		}
	}


	/**
	 * Renders the final {@link WayPoint} of the {@link #path}
	 */
	public void renderFinalWayPoint() {
		if (!path.isEmpty()) {
			try {
				Vector2 waypoint = path.getDestinationWayPoint().waypoint;
				if (waypoint == null) {
					return;
				}
				BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
				Shaders.pass.setUniformMatrix("u_projTrans", UserInterface.UICameraTrackingCam.combined);
				BloodAndMithrilClient.spriteBatch.draw(UserInterface.finalWaypointTexture, waypoint.x - UserInterface.finalWaypointTexture.getRegionWidth()/2, waypoint.y);
			} catch (NullPointerException e) {
				// ???
			}
		}
	}


	/**
	 * Goes to a {@link WayPoint} in the {@link #path}, removing it upon arrival
	 */
	private void goToWayPoint(WayPoint wayPoint, int stuckTolerance) {
		Individual host = Domain.getIndividual(hostId.getId());

		// If we're outside WayPoint.tolerance, then move toward WayPoint.wayPoint
		boolean notReached;
		if (wayPoint.tolerance == 0f) {
			try {
				if (host.getState().position.y < 0f) {
					notReached = !wayPoint.waypoint.equals(Topography.convertToWorldCoord(new Vector2(host.getState().position.x, host.getState().position.y + 1), true));
				} else {
					notReached = !wayPoint.waypoint.equals(Topography.convertToWorldCoord(host.getState().position, true));
				}
			} catch (NoTileFoundException e) {
				return;
			}
		} else {
			notReached = Math.abs(wayPoint.waypoint.cpy().sub(host.getState().position).len()) > wayPoint.tolerance;
		}

		if (notReached) {
			host.sendCommand(getKeyMappings().walk.keyCode, host.isWalking() || host.getState().stamina == 0f);
			host.setWalking(host.isWalking() || host.getState().stamina == 0f);
			if (!host.isCommandActive(getKeyMappings().moveRight.keyCode) && wayPoint.waypoint.x > host.getState().position.x) {
				host.sendCommand(getKeyMappings().moveRight.keyCode, true);
				host.sendCommand(getKeyMappings().moveLeft.keyCode, false);
				stuckCounter++;
			} else if (!host.isCommandActive(getKeyMappings().moveLeft.keyCode) && wayPoint.waypoint.x < host.getState().position.x) {
				host.sendCommand(getKeyMappings().moveRight.keyCode, false);
				host.sendCommand(getKeyMappings().moveLeft.keyCode, true);
				stuckCounter++;
			}

			if (stuckCounter > stuckTolerance) {
				path.clear();
				if (!getHost().attacking() && getHost().isControllable()) {
					getHost().speak("Looks like I'm stuck...", 1500);
				}
			}

		// If we've reached the waypoint, and the next waypoint in the path is non-null, then move to the next one in the path.
		} else if (path.getNextPoint() != null) {
			path.getAndRemoveNextWayPoint();
			stuckCounter = 0;
		}
	}


	/**
	 * Task is complete once all waypoints in the path have been visited
	 */
	@Override
	public boolean isComplete() {
		if (function != null) {
			if (function.call()) {
				return true;
			}
		}

		Individual host = Domain.getIndividual(hostId.getId());

		boolean finalWayPointCheck = false;

		WayPoint finalWayPoint = path.getDestinationWayPoint();

		if (finalWayPoint == null) {
			return path.isEmpty() || finalWayPointCheck;
		} else {
			float distance = Math.abs(host.getState().position.cpy().sub(finalWayPoint.waypoint).len());
			finalWayPointCheck = distance < finalWayPoint.tolerance;
		}

		return path.isEmpty() || finalWayPointCheck;
	}


	public void setPath(Path path) {
		this.path = path;
	}


	public Path getPath() {
		return path;
	}


	@Override
	public boolean uponCompletion() {
		Individual host = Domain.getIndividual(hostId.getId());

		host.sendCommand(getKeyMappings().moveRight.keyCode, false);
		host.sendCommand(getKeyMappings().moveLeft.keyCode, false);
		host.sendCommand(getKeyMappings().walk.keyCode, host.isWalking());
		host.setJumpOffToNull();

		return false;
	}
}