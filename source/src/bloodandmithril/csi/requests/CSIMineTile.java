package bloodandmithril.csi.requests;

import java.util.LinkedList;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.MineTile;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.math.Vector2;

/**
 * A CSI {@link Request} to MineTile
 *
 * @author Matt
 */
public class CSIMineTile implements Request {

	private final int individualId;
	private final Vector2 location;

	/**
	 * Constructor
	 */
	public CSIMineTile(int individualId, Vector2 location) {
		this.individualId = individualId;
		this.location = location;
	}


	@Override
	public Responses respond() {
		Individual individual = GameWorld.individuals.get(individualId);
		individual.getAI().setCurrentTask(
			new MineTile(individual, location)
		);

		Responses responses = new Responses(false, new LinkedList<Response>());
		return responses;
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