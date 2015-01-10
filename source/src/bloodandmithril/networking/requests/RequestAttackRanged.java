package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.requests.SynchronizeIndividual.SynchronizeIndividualResponse;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;

/**
 * {@link Request} for an individual to attack using its equipped ranged weapon
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class RequestAttackRanged implements Request {
	
	private final int individualId;
	private final Vector2 direction;

	/**
	 * Constructor
	 */
	public RequestAttackRanged(int individualId, Vector2 direction) {
		this.individualId = individualId;
		this.direction = direction;
	}
	

	@Override
	public Responses respond() {
		Individual individual = Domain.getIndividual(individualId);
		individual.attackRanged(direction);
		Responses responses = new Responses(false);
		responses.add(new SynchronizeIndividualResponse(individualId, System.currentTimeMillis()));
		responses.add(new RefreshWindows.RefreshWindowsResponse());
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