package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import static bloodandmithril.core.BloodAndMithrilClient.getKeyMappings;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

/**
 * Moves a host to a moving location
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Go to location")
public class GoToMovingLocation extends AITask implements RoutineTask {
	private static final long serialVersionUID = 3940840091194740269L;

	/** The changing destination */
	private final Vector2 destination;
	private final float tolerance;
	private GoToLocation currentGoToLocation;
	private SerializableFunction<Boolean> terminationCondition;

	private SerializableFunction<Boolean> repathCondition;

	@Inject
	GoToMovingLocation() {
		super(null);
		this.tolerance = 0;
		this.destination = null;
	}

	/**
	 * Constructor
	 */
	protected GoToMovingLocation(IndividualIdentifier hostId, Vector2 destination, float tolerance) {
		super(hostId);
		this.destination = destination;
		this.tolerance = tolerance;

		Individual host = Domain.getIndividual(hostId.getId());
		this.currentGoToLocation = goTo(
			host,
			host.getState().position.cpy(),
			new WayPoint(destination),
			false,
			150f,
			true
		);
	}


	/**
	 * Constructor
	 */
	protected GoToMovingLocation(IndividualIdentifier hostId, Vector2 destination, SerializableFunction<Boolean> terminationCondition) {
		super(hostId);
		this.destination = destination;
		this.terminationCondition = terminationCondition;
		this.tolerance = -1f;

		Individual host = Domain.getIndividual(hostId.getId());
		this.currentGoToLocation = goTo(
			host,
			host.getState().position.cpy(),
			new WayPoint(destination),
			false,
			150f,
			true
		);
	}


	/**
	 * Constructor
	 */
	protected GoToMovingLocation(IndividualIdentifier hostId, Vector2 destination, SerializableFunction<Boolean> terminationCondition, SerializableFunction<Boolean> repathCondition) {
		super(hostId);
		this.destination = destination;
		this.terminationCondition = terminationCondition;
		this.repathCondition = repathCondition;
		this.tolerance = -1f;

		Individual host = Domain.getIndividual(hostId.getId());
		this.currentGoToLocation = goTo(
			host,
			host.getState().position.cpy(),
			new WayPoint(destination),
			false,
			150f,
			true
		);
	}


	@Override
	public String getShortDescription() {
		return "Moving";
	}


	@Override
	public boolean isComplete() {
		if (terminationCondition != null) {
			return terminationCondition.call();
		}

		return Domain.getIndividual(hostId.getId()).getDistanceFrom(destination) < tolerance;
	}


	/**
	 * See {@link Path#isDirectlyAboveNext(Vector2)}
	 */
	public boolean isAboveNext(Vector2 location) {
		return this.currentGoToLocation.getPath().isDirectlyAboveNext(location);
	}


	@Override
	public boolean uponCompletion() {
		Individual host = Domain.getIndividual(hostId.getId());

		host.sendCommand(getKeyMappings().moveRight.keyCode, false);
		host.sendCommand(getKeyMappings().moveLeft.keyCode, false);
		host.sendCommand(getKeyMappings().walk.keyCode, host.isWalking());

		return false;
	}


	@Override
	public void execute(float delta) {
		getCurrentGoToLocation().execute(delta);

		if (repathCondition == null ? getCurrentGoToLocation().isComplete() : repathCondition.call()) {
			Individual host = Domain.getIndividual(hostId.getId());
			this.currentGoToLocation = goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(destination),
				false,
				150f,
				true
			);
		}
	}

	public GoToLocation getCurrentGoToLocation() {
		return currentGoToLocation;
	}


	@Override
	public String getDetailedDescription() {
		return getHost().getId().getSimpleName() + " moves to " + currentGoToLocation.getPath().getDestinationWayPoint().waypoint.toString();
	}


	@Override
	public ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine<? extends Visible> routine) {
		return null;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine) {
		return null;
	}
}