package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;

/**
 * Moves a host to a moving location
 *
 * @author Matt
 */
public class GoToMovingLocation extends AITask {
	private static final long serialVersionUID = 3940840091194740269L;

	/** The changing destination */
	private final Vector2 destination;
	private final float tolerance;
	private GoToLocation currentGoToLocation;
	private SerializableFunction<Boolean> terminationCondition;

	private SerializableFunction<Boolean> repathCondition;

	/**
	 * Constructor
	 */
	protected GoToMovingLocation(IndividualIdentifier hostId, Vector2 destination, float tolerance) {
		super(hostId);
		this.destination = destination;
		this.tolerance = tolerance;

		this.currentGoToLocation = new GoToLocation(
			Domain.getIndividuals().get(hostId.getId()),
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

		this.currentGoToLocation = new GoToLocation(
			Domain.getIndividuals().get(hostId.getId()),
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

		this.currentGoToLocation = new GoToLocation(
			Domain.getIndividuals().get(hostId.getId()),
			new WayPoint(destination),
			false,
			150f,
			true
		);
	}


	@Override
	public String getDescription() {
		return "Moving";
	}


	@Override
	public boolean isComplete() {
		if (terminationCondition != null) {
			return terminationCondition.call();
		}

		return Domain.getIndividuals().get(hostId.getId()).getDistanceFrom(destination) < tolerance;
	}


	/**
	 * See {@link Path#isDirectlyAboveNext(Vector2)}
	 */
	public boolean isAboveNext(Vector2 location) {
		return this.currentGoToLocation.getPath().isDirectlyAboveNext(location);
	}


	@Override
	public boolean uponCompletion() {
		Individual host = Domain.getIndividuals().get(hostId.getId());

		host.sendCommand(KeyMappings.moveRight, false);
		host.sendCommand(KeyMappings.moveLeft, false);
		host.sendCommand(KeyMappings.walk, host.isWalking());
		
		return false;
	}


	@Override
	public void execute(float delta) {
		getCurrentGoToLocation().execute(delta);

		if (repathCondition == null ? getCurrentGoToLocation().isComplete() : repathCondition.call()) {
			this.currentGoToLocation = new GoToLocation(
				Domain.getIndividuals().get(hostId.getId()),
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
}