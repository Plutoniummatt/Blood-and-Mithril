package bloodandmithril.character.ai.task;

import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.NextWaypointProvider;
import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.pathfinding.implementations.AStarPathFinder;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Performance;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

/**
 * An {@link AITask} that moves an {@link Individual} to a location through the use of a {@link PathFinder}.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class GoToLocation extends AITask implements NextWaypointProvider {
	private static final long serialVersionUID = -4121947217713585991L;

	/** The {@link Path} this {@link GoToLocation} {@link AITask} will follow */
	private Path path;

	/** Whether or not to fly */
	private final boolean fly;

	/** Used to calculate whether the {@link #host} is stuck */
	private int stuckCounter = 0;

	/** Optional termination function */
	private SerializableFunction<Boolean> function;

	@Inject
	private transient Controls controls;

	/**
	 * Constructor
	 */
	private GoToLocation(final Individual host, final Vector2 start, final WayPoint destination, final boolean fly, final float forceTolerance, final boolean safe) {
		super(host.getId());
		this.fly = fly;

		final int blockspan = host.getHeight()/Topography.TILE_SIZE + (host.getHeight() % Topography.TILE_SIZE == 0 ? 0 : 1) - 1;

		final PathFinder pathFinder = new AStarPathFinder();

		this.path = fly ?
			pathFinder.findShortestPathAir(new WayPoint(start), destination, Domain.getWorld(host.getWorldId())):
			pathFinder.findShortestPathGround(new WayPoint(start), destination, blockspan, safe ? host.getSafetyHeight() : 1000, forceTolerance, Domain.getWorld(host.getWorldId()));
	}


	/**
	 * Constructor
	 */
	private GoToLocation(final Individual host, final Vector2 start, final WayPoint destination, final boolean fly, final SerializableFunction<Boolean> function, final boolean safe) {
		super(host.getId());
		this.fly = fly;
		this.function = function;

		final int blockspan = host.getHeight()/Topography.TILE_SIZE + (host.getHeight() % Topography.TILE_SIZE == 0 ? 0 : 1) - 1;

		final PathFinder pathFinder = new AStarPathFinder();

		this.path = fly ?
			pathFinder.findShortestPathAir(new WayPoint(start), destination, Domain.getWorld(host.getWorldId())):
			pathFinder.findShortestPathGround(new WayPoint(start), destination, blockspan, safe ? host.getSafetyHeight() : 1000, 150f, Domain.getWorld(host.getWorldId()));
	}


	public static GoToLocation goTo(final Individual host, final Vector2 start, final WayPoint destination, final boolean fly, final float forceTolerance, final boolean safe) {
		return new GoToLocation(host, start, destination, fly, forceTolerance, safe);
	}


	public static GoToLocation goToWithTerminationFunction(final Individual host, final Vector2 start, final WayPoint destination, final boolean fly, final SerializableFunction<Boolean> function, final boolean safe) {
		return new GoToLocation(host, start, destination, fly, function, safe);
	}


	/**
	 * True if the {@link #path} contains a {@link WayPoint} representing the {@link Tile} at this location
	 */
	public boolean isPartOfPath(final Vector2 location) {
		return path.isPartOfPathGroundAndIsNext(location);
	}


	/**
	 * See {@link Path#isDirectlyAboveNext(Vector2)}
	 */
	public boolean isAboveNext(final Vector2 location) {
		return path.isDirectlyAboveNext(location);
	}


	@Override
	protected void internalExecute(final float delta) {
		if (!path.isEmpty()) {
			if (fly) {
				// TODO Flying
			} else {
				final Vector2 waypoint = path.getNextPoint().waypoint;
				try {
					if (Domain.getWorld(getHost().getWorldId()).getTopography().getTile(waypoint.x, waypoint.y - Topography.TILE_SIZE / 2, true).isPassable() &&
						Domain.getWorld(getHost().getWorldId()).getTopography().getTile(waypoint.x, waypoint.y - 3 * Topography.TILE_SIZE / 2, true).isPassable() &&
						!Domain.getWorld(getHost().getWorldId()).getTopography().getTile(waypoint.x, waypoint.y - Topography.TILE_SIZE / 2, true).isPlatformTile) {

						getHost().speak("Looks like I'm stuck...", 1500);
						path.clear();
					} else {
						final WayPoint closest = path.getNextPoint();
						int counter = 0;
						int toRemove = 0;
						for(final WayPoint w : path.getWayPoints()) {
							if (counter > 5) {
								break;
							}
							counter++;
							if (getHost().getState().position.dst(w.waypoint) < Topography.TILE_SIZE * 2 && w.waypoint.y < getHost().getState().position.y) {
								path.getAndRemoveNextWayPoint();
								for (int i = 0; i < toRemove; i++) {
									path.getAndRemoveNextWayPoint();
								}
							} else {
								toRemove++;
							}
						}

						goToWayPoint(closest, 4);
					}
				} catch (final NoTileFoundException e) {}
			}
		}
	}


	@Override
	public String getShortDescription() {
		return "Moving";
	}


	/**
	 * Renders the {@link #path} as well as the current trajectory
	 *
	 * Use with care, due to the inefficiency of this method, it is deprecated, intended for debugging purposes only
	 */
	@Performance(explanation = "This method draws a dot and a line between each waypoint, rather inefficient if the path is long")
	public void renderPath() {
		final WayPoint nextPoint = path.getNextPoint();

		if (nextPoint != null && nextPoint.waypoint != null) {
			UserInterface.shapeRenderer.begin(ShapeType.Line);
			UserInterface.shapeRenderer.setColor(Color.WHITE);
			Gdx.gl.glLineWidth(3f);
			final float startX = Domain.getIndividual(hostId.getId()).getState().position.x;
			final float startY = Domain.getIndividual(hostId.getId()).getState().position.y;
			final float endX = nextPoint.waypoint.x;
			final float endY = nextPoint.waypoint.y;

			UserInterface.shapeRenderer.line(
				worldToScreenX(startX),
				worldToScreenY(startY),
				worldToScreenX(endX),
				worldToScreenY(endY)
			);

			UserInterface.shapeRenderer.end();

			path.render();
		}
	}


	/**
	 * Renders the final {@link WayPoint} of the {@link #path}
	 */
	public void renderFinalWayPoint(final Graphics graphics) {
		if (!path.isEmpty()) {
			try {
				final Vector2 waypoint = path.getDestinationWayPoint().waypoint;
				if (waypoint == null) {
					return;
				}
				graphics.getSpriteBatch().setShader(Shaders.pass);
				Shaders.pass.setUniformMatrix("u_projTrans", graphics.getUi().getUITrackingCamera().combined);
				graphics.getSpriteBatch().draw(UserInterface.finalWaypointTexture, waypoint.x - UserInterface.finalWaypointTexture.getRegionWidth()/2, waypoint.y);
			} catch (final NullPointerException e) {
				// ???
			}
		}
	}


	/**
	 * Goes to a {@link WayPoint} in the {@link #path}, removing it upon arrival
	 */
	private void goToWayPoint(final WayPoint wayPoint, final int stuckTolerance) {
		final Individual host = Domain.getIndividual(hostId.getId());

		// If we're outside WayPoint.tolerance, then move toward WayPoint.wayPoint
		boolean notReached;
		if (wayPoint.tolerance == 0f) {
			try {
				if (host.getState().position.y < 0f) {
					notReached = !wayPoint.waypoint.equals(Topography.convertToWorldCoord(new Vector2(host.getState().position.x, host.getState().position.y + 1), true));
				} else {
					notReached = !wayPoint.waypoint.equals(Topography.convertToWorldCoord(host.getState().position, true));
				}
			} catch (final NoTileFoundException e) {
				return;
			}
		} else {
			notReached = Math.abs(wayPoint.waypoint.cpy().sub(host.getState().position).len()) > wayPoint.tolerance;
		}


		if (notReached) {
			host.sendCommand(controls.walk.keyCode, host.isWalking() || host.getState().stamina == 0f);
			host.setWalking(host.isWalking() || host.getState().stamina == 0f);
			if (!host.isCommandActive(controls.moveRight.keyCode) && wayPoint.waypoint.x > host.getState().position.x) {
				host.sendCommand(controls.moveRight.keyCode, true);
				host.sendCommand(controls.moveLeft.keyCode, false);
				stuckCounter++;
			} else if (!host.isCommandActive(controls.moveLeft.keyCode) && wayPoint.waypoint.x < host.getState().position.x) {
				host.sendCommand(controls.moveRight.keyCode, false);
				host.sendCommand(controls.moveLeft.keyCode, true);
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
		if (function != null) {
			if (function.call()) {
				return true;
			}
		}

		final Individual host = Domain.getIndividual(hostId.getId());

		boolean finalWayPointCheck = false;

		final WayPoint finalWayPoint = path.getDestinationWayPoint();

		if (finalWayPoint == null) {
			return path.isEmpty() || finalWayPointCheck;
		} else {
			final float distance = Math.abs(host.getState().position.cpy().sub(finalWayPoint.waypoint).len());
			finalWayPointCheck = distance < finalWayPoint.tolerance;
		}

		return path.isEmpty() || finalWayPointCheck;
	}


	public void setPath(final Path path) {
		this.path = path;
	}


	public Path getPath() {
		return path;
	}


	@Override
	public boolean uponCompletion() {
		final Individual host = Domain.getIndividual(hostId.getId());

		host.sendCommand(controls.moveRight.keyCode, false);
		host.sendCommand(controls.moveLeft.keyCode, false);
		host.sendCommand(controls.walk.keyCode, host.isWalking());
		host.setJumpOffToNull();

		return false;
	}


	@Override
	public WayPoint provideNextWaypoint() {
		return path.getNextPoint();
	}
}