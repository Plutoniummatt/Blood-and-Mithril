package bloodandmithril.networking.requests;

import java.util.Set;

import com.google.common.collect.Sets;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.playerinteraction.individual.api.IndividualAttackOtherService;
import bloodandmithril.world.Domain;

/**
 * {@link Request} for an {@link Individual} to attack another {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class AttackRequest implements Request {

	private Set<Integer> victims = Sets.newHashSet();
	private int attacker;

	/**
	 * Constructor
	 */
	public AttackRequest(Individual attacker, Set<Individual> victims) {
		this.attacker = attacker.getId().getId();
		for (Individual indi : victims) {
			this.victims.add(indi.getId().getId());
		}
	}


	@Override
	public Responses respond() {
		Individual attackingIndividual = Domain.getIndividual(attacker);
		Individual[] toBeAttacked = new Individual[victims.size()];

		int index = 0;
		for (Integer i : victims) {
			toBeAttacked[index] = Domain.getIndividual(i);
			index++;
		}

		if (attackingIndividual == null || toBeAttacked.length == 0) {
			return new Responses(false);
		}

		Wiring.injector().getInstance(IndividualAttackOtherService.class).attack(attackingIndividual, toBeAttacked);

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