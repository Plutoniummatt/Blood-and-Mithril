package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.Follow;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;

/**
 * {@link Request} for an {@link Individual} to follow another
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class FollowRequest implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6303059176298455708L;
	private final int followerId;
	private final int followeeId;
	private final int distance;
	private final SerializableFunction<Boolean> terminationCondition;

	/**
	 * Constructor
	 */
	public FollowRequest(final Individual follower, Individual followee, final int distance, SerializableFunction<Boolean> terminationCondition) {
		this.distance = distance;
		this.terminationCondition = terminationCondition;
		this.followerId = follower.getId().getId();
		this.followeeId = followee.getId().getId();
	}


	@Override
	public Responses respond() {
		Individual follower = Domain.getIndividual(followerId);
		Individual followee = Domain.getIndividual(followeeId);
		follower.getAI().setCurrentTask(new Follow(follower, followee, distance, terminationCondition));

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