package bloodandmithril.playerinteraction.individual.service;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualFollowOtherService;
import bloodandmithril.util.SerializableFunction;

/**
 * Client side implementation of {@link IndividualFollowOtherService}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualFollowOtherServiceClientImpl implements IndividualFollowOtherService {

	@Override
	public void follow(final Individual follower, Individual followee, final int distance, SerializableFunction<Boolean> terminationCondition) {
		ClientServerInterface.SendRequest.sendFollowRequest(follower, followee, distance, terminationCondition);
	}
}