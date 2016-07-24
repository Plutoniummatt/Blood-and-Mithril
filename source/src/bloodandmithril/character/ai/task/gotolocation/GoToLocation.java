package bloodandmithril.character.ai.task.gotolocation;

import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.NextWaypointProvider;
import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.pathfinding.implementations.AStarPathFinder;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Performance;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Shaders;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

/**
 * An {@link AITask} that moves an {@link Individual} to a location through the use of a {@link PathFinder}.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ExecutedBy(GoToLocationExecutor.class)
public class GoToLocation extends AITask implements NextWaypointProvider {
	private static final long serialVersionUID = -4121947217713585991L;
	
	/** The {@link Path} this {@link GoToLocation} {@link AITask} will follow */
	Path path;

	/** Whether or not to fly */
	final boolean fly;

	/** Used to calculate whether the {@link #host} is stuck */
	int stuckCounter = 0;

	/** Optional termination function */
	SerializableFunction<Boolean> function;

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
		UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
		
		final WayPoint nextPoint = path.getNextPoint();

		if (nextPoint != null && nextPoint.waypoint != null) {
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(Color.WHITE);
			Gdx.gl.glLineWidth(3f);
			final float startX = Domain.getIndividual(hostId.getId()).getState().position.x;
			final float startY = Domain.getIndividual(hostId.getId()).getState().position.y;
			final float endX = nextPoint.waypoint.x;
			final float endY = nextPoint.waypoint.y;

			userInterface.getShapeRenderer().line(
				worldToScreenX(startX),
				worldToScreenY(startY),
				worldToScreenX(endX),
				worldToScreenY(endY)
			);

			userInterface.getShapeRenderer().end();

			path.render();
		}
	}


	/**
	 * Renders the final {@link WayPoint} of the {@link #path}
	 */
	public void renderFinalWayPoint(final Graphics graphics) {
		UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
		
		if (!path.isEmpty()) {
			try {
				final Vector2 waypoint = path.getDestinationWayPoint().waypoint;
				if (waypoint == null) {
					return;
				}
				graphics.getSpriteBatch().setShader(Shaders.pass);
				Shaders.pass.setUniformMatrix("u_projTrans", userInterface.getUITrackingCamera().combined);
				graphics.getSpriteBatch().draw(UserInterface.finalWaypointTexture, waypoint.x - UserInterface.finalWaypointTexture.getRegionWidth()/2, waypoint.y);
			} catch (final NullPointerException e) {
				// ???
			}
		}
	}


	public void setPath(final Path path) {
		this.path = path;
	}


	public Path getPath() {
		return path;
	}


	@Override
	public WayPoint provideNextWaypoint() {
		return path.getNextPoint();
	}
}