package bloodandmithril.networking.requests;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to move an {@link Individual}
 */
@Copyright("Matthew Peck 2014")
public class MoveIndividual implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3018948370620961122L;

	/** id of the {@link Individual} to be moved */
	public final int individualId;

	/** Coordinates of the destination */
	public final Vector2 destinationCoordinates;

	/** Whether or not to force move */
	private final boolean forceMove;

	/** Whether to add to existing task */
	private final boolean add;

	/** Whether to jump */
	private final boolean jump;

	/** Coordinates of the jump destination */
	private final Vector2 jumpFrom, jumpCoords;

	@Inject
	private transient GameClientStateTracker gameClientStateTracker;

	/**
	 * Constructor
	 */
	public MoveIndividual(final int individualId, final Vector2 destinationCoordinates, final boolean forceMove, final boolean add, final boolean jump, final Vector2 jumpFrom, final Vector2 jumpCoords) {
		this.individualId = individualId;
		this.destinationCoordinates = destinationCoordinates;
		this.forceMove = forceMove;
		this.add = add;
		this.jump = jump;
		this.jumpFrom = jumpFrom;
		this.jumpCoords = jumpCoords;
	}


	@Override
	public Responses respond() {
		final Individual individual = Domain.getIndividual(individualId);
		if (individual != null && gameClientStateTracker.isIndividualSelected(individual)) {
			if (jump) {
				AIProcessor.sendJumpResolutionRequest(
					individual,
					jumpFrom,
					jumpCoords,
					add
				);
			} else {
				AIProcessor.sendPathfindingRequest(
					individual,
					new WayPoint(destinationCoordinates),
					false,
					150f,
					!forceMove,
					add
				);
			}
		}

		return new Response.Responses(false);
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}
}