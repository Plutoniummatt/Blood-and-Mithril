package bloodandmithril.character.ai.task;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.pathfinding.implementations.AStarPathFinder;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

/**
 * An {@link AITask} that moves an {@link Individual} to a location through the use of a {@link PathFinder}.
 *
 * @author Matt
 */
public class GoToLocation extends AITask {
	private static final long serialVersionUID = -4121947217713585991L;

	/** The {@link Path} this {@link GoToLocation} {@link AITask} will follow */
	private Path path;

	/** Whether or not to fly */
	private final boolean fly;

	/** Used to calculate whether the {@link #host} is stuck */
	private int stuckCounter = 0;

	/**
	 * Constructor
	 */
	public GoToLocation(Individual host, WayPoint destination, boolean fly, float forceTolerance, boolean safe) {
		super(host.getId());
		this.fly = fly;

		int blockspan = host.getHeight()/Topography.TILE_SIZE + (host.getHeight() % Topography.TILE_SIZE == 0 ? 0 : 1) - 1;

		PathFinder pathFinder = new AStarPathFinder();

		this.path = fly ?
			pathFinder.findShortestPathAir(new WayPoint(host.getState().position), destination, Domain.getWorld(host.getWorldId())):
			pathFinder.findShortestPathGround(new WayPoint(host.getState().position), destination, blockspan, safe ? host.getSafetyHeight() : 1000, forceTolerance, Domain.getWorld(host.getWorldId()));
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
				goToWayPoint(path.getNextPoint(), 4);
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
	@Deprecated
	public void renderPath() {
		WayPoint nextPoint = path.getNextPoint();

		if (nextPoint != null && nextPoint.waypoint != null) {
			UserInterface.shapeRenderer.begin(ShapeType.Line);
			float startX = Domain.getIndividuals().get(hostId.getId()).getState().position.x;
			float startY = Domain.getIndividuals().get(hostId.getId()).getState().position.y;
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
			Vector2 waypoint = path.getDestinationWayPoint().waypoint;
			if (waypoint == null) {
				return;
			}
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
			Shaders.pass.setUniformMatrix("u_projTrans", UserInterface.UICameraTrackingCam.combined);
			BloodAndMithrilClient.spriteBatch.draw(UserInterface.finalWaypointTexture, waypoint.x - UserInterface.finalWaypointTexture.getRegionWidth()/2, waypoint.y);
		}
	}


	/**
	 * Goes to a {@link WayPoint} in the {@link #path}, removing it upon arrival
	 */
	private void goToWayPoint(WayPoint wayPoint, int stuckTolerance) {
		Individual host = Domain.getIndividuals().get(hostId.getId());

		// If we're outside WayPoint.tolerance, then move toward WayPoint.wayPoint
		boolean notReached;
		if (wayPoint.tolerance == 0f) {
			if (host.getState().position.y < 0f) {
				notReached = !wayPoint.waypoint.equals(Topography.convertToWorldCoord(new Vector2(host.getState().position.x, host.getState().position.y + 1), true));
			} else {
				notReached = !wayPoint.waypoint.equals(Topography.convertToWorldCoord(host.getState().position, true));
			}
		} else {
			notReached = Math.abs(wayPoint.waypoint.cpy().sub(host.getState().position).len()) > wayPoint.tolerance;
		}

		if (notReached) {
			host.sendCommand(KeyMappings.walk, host.isWalking() || host.getState().stamina == 0f);
			host.setWalking(host.isWalking() || host.getState().stamina == 0f);
			if (!host.isCommandActive(KeyMappings.moveRight) && wayPoint.waypoint.x > host.getState().position.x) {
				host.sendCommand(KeyMappings.moveRight, true);
				host.sendCommand(KeyMappings.moveLeft, false);
				stuckCounter++;
			} else if (!host.isCommandActive(KeyMappings.moveLeft) && wayPoint.waypoint.x < host.getState().position.x) {
				host.sendCommand(KeyMappings.moveRight, false);
				host.sendCommand(KeyMappings.moveLeft, true);
				stuckCounter++;
			}

			if (stuckCounter > stuckTolerance) {
				path.clear();
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
		Individual host = Domain.getIndividuals().get(hostId.getId());

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
	public void uponCompletion() {
		Individual host = Domain.getIndividuals().get(hostId.getId());

		host.sendCommand(KeyMappings.moveRight, false);
		host.sendCommand(KeyMappings.moveLeft, false);
		host.sendCommand(KeyMappings.walk, host.isWalking());
		host.setJumpOffToNull();
	}
}