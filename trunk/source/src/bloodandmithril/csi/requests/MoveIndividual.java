package bloodandmithril.csi.requests;

import java.util.LinkedList;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.math.Vector2;

/**
 * {@link Request} to move an {@link Individual}
 */
public class MoveIndividual implements Request {

	/** id of the {@link Individual} to be moved */
	public final int individualId;

	/** Coordinates of the destination */
	public final Vector2 destinationCoordinates;

	/** Whether or not to force move */
	private final boolean forceMove;

	/**
	 * Constructor
	 */
	public MoveIndividual(int individualId, Vector2 destinationCoordinates, boolean forceMove) {
		this.individualId = individualId;
		this.destinationCoordinates = destinationCoordinates;
		this.forceMove = forceMove;
	}


	@Override
	public Responses respond() {
		Individual individual = GameWorld.individuals.get(individualId);
		if (individual != null && GameWorld.selectedIndividuals.contains(individual)) {
			AIProcessor.sendPathfindingRequest(
					individual,
				new WayPoint(destinationCoordinates),
				false,
				150f,
				!forceMove
			);
		}

		Response response = new MoveIndividualResponse();
		Responses responses = new Response.Responses(false, new LinkedList<Response>());
		responses.responses.add(response);
		return responses;
	}


	@Override
	public boolean tcp() {
		return false;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}


	public static class MoveIndividualResponse implements Response {
		@Override
		public void acknowledge() {
		}

		@Override
		public int forClient() {
			return -1;
		}

		@Override
		public void prepare() {
		}
	}
}