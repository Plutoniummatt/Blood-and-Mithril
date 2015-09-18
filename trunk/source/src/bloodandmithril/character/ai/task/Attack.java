package bloodandmithril.character.ai.task;

import static bloodandmithril.core.BloodAndMithrilClient.getKeyMappings;
import static bloodandmithril.util.Util.transformSet;
import static bloodandmithril.world.Domain.getIndividual;

import java.util.Set;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.combat.CombatService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
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
@Copyright("Matthew Peck 2014")
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
		host.setCombatStance(true);

		for (Individual individual : toBeAttacked) {
			this.toBeAttacked.add(individual.getId().getId());
		}

		Individual alive = getAlive();

		if (alive != null) {
			Vector2 location = alive.getState().position;

			if (alive.canBeAttacked(host)) {
				appendTask(new GoToMovingLocation(
					hostId,
					location,
					new WithinAttackRangeOrCantAttack(hostId, alive.getId())
				));

				appendTask(new AttackTarget(host.getId(), alive.getId().getId()));
			} else {
				host.addFloatingText("Waiting...", Color.ORANGE);
				appendTask(new Follow(host, alive, 8, new Countdown(3000)));
				appendTask(new ReevaluateAttack(hostId));
			}
		} else {
			appendTask(new Idle());
		}
	}


	/**
	 * Constructor
	 */
	public Attack(IndividualIdentifier hostId, Set<Integer> toBeAttacked) {
		this(Domain.getIndividual(hostId.getId()), transformSet(toBeAttacked, id -> {return Domain.getIndividual(id);}));
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
		Individual host = Domain.getIndividual(hostId.getId());
		host.sendCommand(getKeyMappings().moveRight.keyCode, false);
		host.sendCommand(getKeyMappings().moveLeft.keyCode, false);
		return false;
	}


	private Individual getAlive() {
		for (Integer id : toBeAttacked) {
			Individual individual = Domain.getIndividual(id);
			if (individual != null && individual.getState().health > 0f) {
				return individual;
			}
		}

		return null;
	}


	public Set<Integer> getTargets() {
		return toBeAttacked;
	}


	public class WithinAttackRangeOrCantAttack implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -927709499203093624L;
		private IndividualIdentifier attacker;
		private IndividualIdentifier attackee;

		/**
		 * Constructor
		 */
		public WithinAttackRangeOrCantAttack(IndividualIdentifier attacker, IndividualIdentifier attackee) {
			this.attacker = attacker;
			this.attackee = attackee;
		}


		@Override
		public Boolean call() {
			Individual atker = getIndividual(attacker.getId());
			Individual victim = getIndividual(attackee.getId());

			boolean closeEnough = false;
			AITask subTask = getCurrentTask();
			if (subTask instanceof GoToMovingLocation) {
				int size = ((GoToMovingLocation) subTask).getCurrentGoToLocation().getPath().getSize();
				closeEnough = size < 8;
			}

			return CombatService.getAttackingHitBox(atker).overlapsWith(victim.getHitBox()) || closeEnough && !victim.canBeAttacked(atker);
		}
	}


	/**
	 * Re-evaluates whether an individual can attack another
	 *
	 * @author Matt
	 */
	public class ReevaluateAttack extends AITask {
		private static final long serialVersionUID = 7740671063229381512L;

		/**
		 * Constructor
		 */
		public ReevaluateAttack(IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getDescription() {
			return "";
		}


		@Override
		public boolean isComplete() {
			return true;
		}


		@Override
		public boolean uponCompletion() {
			Domain.getIndividual(hostId.getId()).getAI().setCurrentTask(new Attack(hostId, toBeAttacked));
			return false;
		}


		@Override
		public void execute(float delta) {
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
		private int target;

		protected AttackTarget(IndividualIdentifier hostId, int target) {
			super(hostId);
			this.target = target;
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
			Domain.getIndividual(hostId.getId()).getAI().setCurrentTask(new Attack(hostId, toBeAttacked));
			return false;
		}


		@Override
		public void execute(float delta) {
			Individual alive = getAlive();
			if (alive == null) {
				return;
			}

			Individual attacker = Domain.getIndividual(hostId.getId());
			if (!alive.canBeAttacked(attacker)) {
				complete = true;
				return;
			} else {
				alive.addAttacker(attacker);
			}

			if (new WithinAttackRangeOrCantAttack(hostId, alive.getId()).call()) {
				complete = CombatService.attack(attacker, Sets.newHashSet(target));
			} else {
				complete = true;
			}
		}
	}
}