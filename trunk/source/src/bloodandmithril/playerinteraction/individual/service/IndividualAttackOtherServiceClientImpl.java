package bloodandmithril.playerinteraction.individual.service;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.playerinteraction.individual.api.IndividualAttackOtherService;

/**
 * Client side implementation of {@link IndividualAttackOtherService}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualAttackOtherServiceClientImpl implements IndividualAttackOtherService {

	@Override
	public void attack(Individual attacker, Individual... victim) {
		ClientServerInterface.SendRequest.sendRequestAttack(attacker, victim);
	}
}