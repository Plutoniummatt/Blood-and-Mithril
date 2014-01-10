package bloodandmithril.csi.requests;

import java.util.List;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

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
	public List<Response> respond() {
		Individual individual = GameWorld.individuals.get(individualId);
		if (individual != null && individual.selected) {
			AIProcessor.sendPathfindingRequest(
					individual,
				new WayPoint(destinationCoordinates),
				false,
				150f,
				!forceMove
			);
		}
		Response response = new MoveIndividualResponse();
		return Lists.newArrayList(response);
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}


	public static class MoveIndividualResponse implements Response {
		@Override
		public void acknowledge() {
		}
	}
}