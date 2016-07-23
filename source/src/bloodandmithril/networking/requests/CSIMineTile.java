package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.minetile.MineTile;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;

/**
 * A CSI {@link Request} to MineTile
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class CSIMineTile implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3777591600703104727L;
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
		Individual individual = Domain.getIndividual(individualId);
		individual.getAI().setCurrentTask(
			new MineTile(individual, location)
		);

		Responses responses = new Responses(false);
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