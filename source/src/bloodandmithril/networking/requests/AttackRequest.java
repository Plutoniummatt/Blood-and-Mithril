package bloodandmithril.networking.requests;

import java.util.Set;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
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
	private static final long serialVersionUID = -6580583556083047964L;

	@Inject
	private transient IndividualAttackOtherService individualAttackOtherService;

	private Set<Integer> victims = Sets.newHashSet();
	private int attacker;

	/**
	 * Constructor
	 */
	public AttackRequest(final Individual attacker, final Set<Individual> victims) {
		this.attacker = attacker.getId().getId();
		for (final Individual indi : victims) {
			this.victims.add(indi.getId().getId());
		}
	}


	@Override
	public Responses respond() {
		final Individual attackingIndividual = Domain.getIndividual(attacker);
		final Individual[] toBeAttacked = new Individual[victims.size()];

		int index = 0;
		for (final Integer i : victims) {
			toBeAttacked[index] = Domain.getIndividual(i);
			index++;
		}

		if (attackingIndividual == null || toBeAttacked.length == 0) {
			return new Responses(false);
		}

		individualAttackOtherService.attack(attackingIndividual, toBeAttacked);

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