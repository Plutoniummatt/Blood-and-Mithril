package bloodandmithril.playerinteraction.individual.api;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Service for changing the nick name of an {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface IndividualChangeNicknameService {

	public void changeNickname(Individual individual, String toChangeTo);
}