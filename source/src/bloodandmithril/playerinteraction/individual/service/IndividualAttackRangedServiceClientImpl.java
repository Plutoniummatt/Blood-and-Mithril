package bloodandmithril.playerinteraction.individual.service;

import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualAttackRangedService;

/**
 * Client-side implementation of {@link IndividualAttackRangedService}
 *
 * @author mattp
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class IndividualAttackRangedServiceClientImpl implements IndividualAttackRangedService {

	/**
	 * @see bloodandmithril.playerinteraction.individual.api.IndividualAttackRangedService#attack(bloodandmithril.character.individuals.Individual, com.badlogic.gdx.math.Vector2)
	 */
	@Override
	public void attack(final Individual individual, final Vector2 direction) {
		ClientServerInterface.SendRequest.sendAttackRangedRequest(individual, new Vector2(getMouseWorldX(), getMouseWorldY()));
	}
}