package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.IndividualIdentifier;
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
	private SerializableFunction<Boolean> condition;
	private boolean terminates;

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
	protected GoToMovingLocation(IndividualIdentifier hostId, Vector2 destination, SerializableFunction<Boolean> condition, boolean terminates) {
		super(hostId);
		this.destination = destination;
		this.condition = condition;
		this.terminates = terminates;
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
	protected GoToMovingLocation(IndividualIdentifier hostId, Vector2 destination, SerializableFunction<Boolean> condition, SerializableFunction<Boolean> repathCondition, boolean terminates) {
		super(hostId);
		this.destination = destination;
		this.condition = condition;
		this.repathCondition = repathCondition;
		this.terminates = terminates;
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
		if (condition != null) {
			return condition.call() && terminates;
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
		Domain.getIndividuals().get(hostId.getId()).clearCommands();
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