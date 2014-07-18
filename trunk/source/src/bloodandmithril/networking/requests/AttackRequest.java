package bloodandmithril.networking.requests;

import java.util.Set;

import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

import com.google.common.collect.Sets;

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
		Individual attackingIndividual = Domain.getIndividuals().get(attacker);
		Set<Individual> toBeAttacked = Sets.newHashSet();

		for (Integer i : victims) {
			toBeAttacked.add(Domain.getIndividuals().get(i));
		}

		if (attackingIndividual == null || toBeAttacked.isEmpty()) {
			return new Responses(false);
		}

		attackingIndividual.getAI().setCurrentTask(new Attack(attackingIndividual, toBeAttacked));

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