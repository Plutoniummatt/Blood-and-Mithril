package bloodandmithril.character.ai.task;

import java.util.Set;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.ui.KeyMappings;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;

/**
 * {@link AITask} that:
 *
 * Makes an {@link Individual} move towards another
 * Attack when within range, until the target is dead, continuously moving into attacking range when necessary
 *
 * @author Matt
 */
public class Attack extends CompositeAITask {
	private static final long serialVersionUID = 1106295624210596573L;

	private Set<Integer> toBeAttacked = Sets.newHashSet();

	/**
	 * Constructor
	 */
	public Attack(Individual host, Individual... toBeAttacked) {
		this(host, Sets.newHashSet(toBeAttacked));
	}


	/**
	 * Constructor
	 */
	public Attack(Individual host, Set<Individual> toBeAttacked) {
		super(host.getId(), "Attacking");

		for (Individual individual : toBeAttacked) {
			this.toBeAttacked.add(individual.getId().getId());
		}

		Individual alive = getAlive();

		if (alive != null) {
			Vector2 location = alive.getState().position;

			appendTask(new GoToMovingLocation(
				hostId,
				location,
				new WithinAttackRange(hostId, alive.getId()),
				true
			));

			appendTask(new AttackTarget(host.getId()));
		} else {
			appendTask(new Idle());
		}
	}


	/**
	 * Constructor
	 */
	public Attack(IndividualIdentifier hostId, Set<Integer> toBeAttacked) {
		super(hostId, "Attacking");

		this.toBeAttacked.clear();
		this.toBeAttacked.addAll(toBeAttacked);

		Individual alive = getAlive();
		if (alive != null) {
			Domain.getIndividuals().get(hostId.getId()).setCombatStance(true);
			Vector2 location = alive.getState().position;

			appendTask(new GoToMovingLocation(
				hostId,
				location,
				new WithinAttackRange(hostId, alive.getId()),
				true
			));

			appendTask(new AttackTarget(hostId));
		} else {
			appendTask(new Idle());
		}
	}


	@Override
	public String getDescription() {
		return "Attacking";
	}


	@Override
	public boolean isComplete() {
		return getAlive() == null;
	}


	@Override
	public boolean uponCompletion() {
		Individual host = Domain.getIndividuals().get(hostId.getId());
		host.sendCommand(KeyMappings.moveRight, false);
		host.sendCommand(KeyMappings.moveLeft, false);
		return false;
	}


	private Individual getAlive() {
		for (Integer id : toBeAttacked) {
			Individual individual = Domain.getIndividuals().get(id);
			if (individual.getState().health > 0f) {
				return individual;
			}
		}

		return null;
	}


	public static class WithinAttackRange implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -927709499203093624L;
		private IndividualIdentifier attacker;
		private IndividualIdentifier attackee;

		/**
		 * Constructor
		 */
		public WithinAttackRange(IndividualIdentifier attacker, IndividualIdentifier attackee) {
			this.attacker = attacker;
			this.attackee = attackee;
		}


		@Override
		public Boolean call() {
			return Domain.getIndividuals().get(attacker.getId()).getAttackingHitBox().overlapsWith(
				Domain.getIndividuals().get(attackee.getId()).getHitBox()
			);
		}
	}


	/**
	 * AITask representing the actual attack
	 *
	 * @author Matt
	 */
	public class AttackTarget extends AITask {
		private static final long serialVersionUID = -6824012439864939617L;
		private boolean complete;

		protected AttackTarget(IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getDescription() {
			return "Attacking";
		}


		@Override
		public boolean isComplete() {
			return complete;
		}


		@Override
		public boolean uponCompletion() {
			Domain.getIndividuals().get(hostId.getId()).getAI().setCurrentTask(new Attack(hostId, toBeAttacked));
			return false;
		}


		@Override
		public void execute(float delta) {
			Individual alive = getAlive();
			if (alive == null) {
				return;
			}

			if (new WithinAttackRange(hostId, alive.getId()).call()) {
				complete = Domain.getIndividuals().get(hostId.getId()).attack(toBeAttacked);
			} else {
				complete = true;
			}
		}
	}
}