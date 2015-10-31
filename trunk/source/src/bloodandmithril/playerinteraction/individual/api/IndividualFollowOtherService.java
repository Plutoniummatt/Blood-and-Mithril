package bloodandmithril.playerinteraction.individual.api;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableFunction;

/**
 * Service for instructing an {@link Individual} to follow another
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface IndividualFollowOtherService {

	public void follow(final Individual follower, Individual followee, final int distance, SerializableFunction<Boolean> terminationCondition);
}