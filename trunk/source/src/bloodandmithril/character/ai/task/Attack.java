package bloodandmithril.character.ai.task;

import static bloodandmithril.util.Util.transformSet;
import static bloodandmithril.world.Domain.getIndividuals;

import java.util.Set;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.ui.KeyMappings;
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

				appendTask(new AttackTarget(host.getId()));
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
		this(Domain.getIndividuals().get(hostId.getId()), transformSet(toBeAttacked, id -> {return Domain.getIndividuals().get(id);}));
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
			Individual atker = getIndividuals().get(attacker.getId());
			Individual victim = getIndividuals().get(attackee.getId());
			
			boolean closeEnough = false;
			AITask subTask = getCurrentTask();
			if (subTask instanceof GoToMovingLocation) {
				closeEnough = ((GoToMovingLocation) subTask).getCurrentGoToLocation().getPath().getSize() < 9;
			}
			
			return atker.getAttackingHitBox().overlapsWith(
				victim.getHitBox()
			) || (closeEnough && !victim.canBeAttacked(atker));
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
			Domain.getIndividuals().get(hostId.getId()).getAI().setCurrentTask(new Attack(hostId, toBeAttacked));
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

			Individual attacker = Domain.getIndividuals().get(hostId.getId());
			if (!alive.canBeAttacked(attacker)) {
				complete = true;
				return;
			} else {
				alive.addAttacker(attacker);
			}

			if (new WithinAttackRangeOrCantAttack(hostId, alive.getId()).call()) {
				complete = attacker.attack(toBeAttacked);
			} else {
				complete = true;
			}
		}
	}


	public static class Countdown implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -5761537304910257687L;
		private final long startTime;
		private final long duration;

		public Countdown(long duration) {
			this.duration = duration;
			this.startTime = System.currentTimeMillis();
		}

		@Override
		public Boolean call() {
			return System.currentTimeMillis() - duration >= startTime;
		}
	}
}