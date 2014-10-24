package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.Follow;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * {@link Request} for an {@link Individual} to follow another
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class FollowRequest implements Request {

	private final int followerId;
	private final int followeeId;

	/**
	 * Constructor
	 */
	public FollowRequest(Individual follower, Individual followee) {
		this.followerId = follower.getId().getId();
		this.followeeId = followee.getId().getId();
	}


	@Override
	public Responses respond() {
		Individual follower = Domain.getIndividual(followerId);
		Individual followee = Domain.getIndividual(followeeId);
		follower.getAI().setCurrentTask(new Follow(follower, followee, 10, null));

		return new Responses(false);
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