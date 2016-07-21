package bloodandmithril.networking.requests;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.requests.SynchronizeIndividual.SynchronizeIndividualResponse;
import bloodandmithril.world.Domain;

/**
 * {@link Request} for an individual to attack using its equipped ranged weapon
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class RequestAttackRanged implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1811765430701225537L;
	private final int individualId;
	private final Vector2 direction;

	/**
	 * Constructor
	 */
	public RequestAttackRanged(final int individualId, final Vector2 direction) {
		this.individualId = individualId;
		this.direction = direction;
	}


	@Override
	public Responses respond() {
		final Individual individual = Domain.getIndividual(individualId);
		individual.attackRanged(direction);
		final Responses responses = new Responses(false);
		responses.add(new SynchronizeIndividualResponse(individualId, System.currentTimeMillis()));
		responses.add(new RefreshWindowsResponse());
		ClientServerInterface.SendNotification.notifySyncProjectiles(individual.getWorldId());

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
}