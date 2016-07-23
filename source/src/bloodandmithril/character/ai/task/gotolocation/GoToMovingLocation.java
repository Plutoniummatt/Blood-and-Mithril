package bloodandmithril.character.ai.task.gotolocation;

import static bloodandmithril.character.ai.task.gotolocation.GoToLocation.goTo;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.NextWaypointProvider;
import bloodandmithril.character.ai.RoutineContextMenusProvidedBy;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;

/**
 * Moves a host to a moving location
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Go to location")
@ExecutedBy(GoToMovingLocationExecutor.class)
@RoutineContextMenusProvidedBy(GoToMovingLocationRoutineContextMenuProvider.class)
public class GoToMovingLocation extends AITask implements RoutineTask, NextWaypointProvider {
	private static final long serialVersionUID = 3940840091194740269L;

	/** The changing destination */
	final SerializableFunction<Vector2> destination;
	final float tolerance;
	GoToLocation currentGoToLocation;
	SerializableFunction<Boolean> terminationCondition;
	SerializableFunction<Boolean> repathCondition;

	@Inject
	@Deprecated
	GoToMovingLocation() {
		super(null);
		this.tolerance = 0;
		this.destination = null;
	}

	/**
	 * Constructor
	 */
	public GoToMovingLocation(final IndividualIdentifier hostId, final SerializableFunction<Vector2> destination, final float tolerance) {
		super(hostId);
		this.destination = destination;
		this.tolerance = tolerance;

		final Individual host = Domain.getIndividual(hostId.getId());
		this.currentGoToLocation = goTo(
			host,
			host.getState().position.cpy(),
			new WayPoint(destination.call()),
			false,
			150f,
			true
		);
	}


	/**
	 * Constructor
	 */
	public GoToMovingLocation(final IndividualIdentifier hostId, final SerializableFunction<Vector2> destination, final SerializableFunction<Boolean> terminationCondition) {
		super(hostId);
		this.destination = destination;
		this.terminationCondition = terminationCondition;
		this.tolerance = -1f;

		final Individual host = Domain.getIndividual(hostId.getId());
		this.currentGoToLocation = goTo(
			host,
			host.getState().position.cpy(),
			new WayPoint(destination.call()),
			false,
			150f,
			true
		);
	}


	/**
	 * Constructor
	 */
	public GoToMovingLocation(final IndividualIdentifier hostId, final SerializableFunction<Vector2> destination, final SerializableFunction<Boolean> terminationCondition, final SerializableFunction<Boolean> repathCondition) {
		super(hostId);
		this.destination = destination;
		this.terminationCondition = terminationCondition;
		this.repathCondition = repathCondition;
		this.tolerance = -1f;

		final Individual host = Domain.getIndividual(hostId.getId());
		this.currentGoToLocation = goTo(
			host,
			host.getState().position.cpy(),
			new WayPoint(destination.call()),
			false,
			150f,
			true
		);
	}


	@Override
	public String getShortDescription() {
		return "Moving";
	}


	/**
	 * See {@link Path#isDirectlyAboveNext(Vector2)}
	 */
	public boolean isAboveNext(final Vector2 location) {
		return this.currentGoToLocation.getPath().isDirectlyAboveNext(location);
	}


	public GoToLocation getCurrentGoToLocation() {
		return currentGoToLocation;
	}


	public static class GoToMovingLocationTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = -2000677075433369539L;
		private float x, y;
		private int hostId;

		public GoToMovingLocationTaskGenerator(final float x, final float y, final int hostId) {
			this.x = x;
			this.y = y;
			this.hostId = hostId;
		}

		@Override
		public AITask apply(final Object input) {
			final Individual individual = Domain.getIndividual(hostId);
			return goTo(
				individual,
				individual.getState().position.cpy(),
				new WayPoint(new Vector2(x, y)),
				false,
				150f,
				true
			);
		}
		@Override
		public String getDailyRoutineDetailedDescription() {
			return "Go to location at: " + String.format("%.1f", x) + ", " + String.format("%.1f", y);
		}
		@Override
		public String getEntityVisibleRoutineDetailedDescription() {
			return "Go to location at: " + String.format("%.1f", x) + ", " + String.format("%.1f", y);
		}
		@Override
		public String getIndividualConditionRoutineDetailedDescription() {
			return "Go to location at: " + String.format("%.1f", x) + ", " + String.format("%.1f", y);
		}
		@Override
		public String getStimulusDrivenRoutineDetailedDescription() {
			return "Go to location at: " + String.format("%.1f", x) + ", " + String.format("%.1f", y);
		}
		@Override
		public boolean valid() {
			return true;
		}
		@Override
		public void render() {
			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			Gdx.gl20.glLineWidth(2f);
			userInterface.getShapeRenderer().setColor(Color.GREEN);
			final Individual attacker = Domain.getIndividual(hostId);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);

			userInterface.getShapeRenderer().setColor(Color.RED);
			userInterface.getShapeRenderer().circle(worldToScreenX(x), worldToScreenY(y), 6f);
			userInterface.getShapeRenderer().end();
		}
	}


	@Override
	public WayPoint provideNextWaypoint() {
		return getCurrentGoToLocation().getPath().getNextPoint();
	}
}