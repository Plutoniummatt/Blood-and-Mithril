package bloodandmithril.character.ai.task;

import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
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

	/**
	 * Constructor
	 */
	protected GoToMovingLocation(IndividualIdentifier hostId, Vector2 destination, float tolerance) {
		super(hostId);
		this.destination = destination;
		this.tolerance = tolerance;

		currentGoToLocation = new GoToLocation(
			Domain.individuals.get(hostId.getId()),
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
		return Domain.individuals.get(hostId.getId()).getDistanceFrom(destination) < tolerance;
	}


	@Override
	public void uponCompletion() {
	}


	@Override
	public void execute() {
		currentGoToLocation.execute();
		if (currentGoToLocation.isComplete()) {
			currentGoToLocation = new GoToLocation(
				Domain.individuals.get(hostId.getId()),
				new WayPoint(destination),
				false,
				150f,
				true
			);
		}
	}
}