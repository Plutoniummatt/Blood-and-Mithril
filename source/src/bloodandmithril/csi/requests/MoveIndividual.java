package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

/**
 * {@link Request} to move an {@link Individual}
 */
public class MoveIndividual implements Request {

	/** id of the {@link Individual} to be moved */
	public final int individualId;

	/** Coordinates of the destination */
	public final Vector2 destinationCoordinates;

	/**
	 * Constructor
	 */
	public MoveIndividual(int individualId, Vector2 destinationCoordinates) {
		this.individualId = individualId;
		this.destinationCoordinates = destinationCoordinates;
	}


	@Override
	public Response respond() {
		Individual individual = GameWorld.individuals.get(individualId);
		if (individual != null && individual.selected) {
			AIProcessor.sendPathfindingRequest(
					individual,
				new WayPoint(destinationCoordinates),
				false,
				150f,
				Gdx.input.isKeyPressed(KeyMappings.forceMove) ? false : true
			);
		}
		return new MoveIndividualResponse();
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