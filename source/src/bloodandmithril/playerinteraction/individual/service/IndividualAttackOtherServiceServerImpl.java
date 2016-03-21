package bloodandmithril.playerinteraction.individual.service;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.playerinteraction.individual.api.IndividualAttackOtherService;

/**
 * Server side implementation of {@link IndividualAttackOtherService}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2015")
public class IndividualAttackOtherServiceServerImpl implements IndividualAttackOtherService {

	@Override
	public void attack(Individual attacker, Individual... victim) {
		attacker.getAI().setCurrentTask(
			new Attack(attacker, victim)
		);
	}
}