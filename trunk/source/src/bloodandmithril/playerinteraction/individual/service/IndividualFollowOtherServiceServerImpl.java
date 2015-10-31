package bloodandmithril.playerinteraction.individual.service;

import bloodandmithril.character.ai.task.Follow;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.playerinteraction.individual.api.IndividualFollowOtherService;
import bloodandmithril.util.SerializableFunction;

/**
 * Server side implementation of {@link IndividualFollowOtherService}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualFollowOtherServiceServerImpl implements IndividualFollowOtherService {

	@Override
	public void follow(final Individual follower, Individual followee, final int distance, SerializableFunction<Boolean> terminationCondition) {
		follower.getAI().setCurrentTask(
			new Follow(follower, followee, 10, null)
		);
	}
}