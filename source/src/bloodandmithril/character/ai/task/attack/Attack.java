package bloodandmithril.character.ai.task.attack;

import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static bloodandmithril.util.Util.transformSet;
import static bloodandmithril.world.Domain.getIndividual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AIProcessor.ReturnIndividualPosition;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.RoutineContextMenusProvidedBy;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine.VisibleIndividualFuture;
import bloodandmithril.character.ai.task.GoToMovingLocation;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.follow.Follow;
import bloodandmithril.character.combat.CombatService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

/**
 * {@link AITask} that:
 *
 * Makes an {@link Individual} move towards another
 * Attack when within range, until the target is dead, continuously moving into attacking range when necessary
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Attack")
@ExecutedBy(AttackExecutor.class)
@RoutineContextMenusProvidedBy(AttackRoutineContextMenuProvider.class)
public final class Attack extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = 1106295624210596573L;

	private final HashSet<Integer> toBeAttacked = Sets.newHashSet();

	/**
	 * This is used to create routines only
	 */
	@Inject
	@Deprecated
	Attack() {
		super(null, "");
	}

	/**
	 * Constructor
	 */
	public Attack(final Individual host, final Individual... toBeAttacked) {
		this(host, Sets.newHashSet(toBeAttacked));
	}

	/**
	 * Constructor
	 */
	public Attack(final Individual host, final Set<Individual> toBeAttacked) {
		super(host.getId(), "Attacking");

		for (final Individual individual : toBeAttacked) {
			this.toBeAttacked.add(individual.getId().getId());
		}

		final Individual alive = getAlive();

		if (alive != null) {
			if (alive.canBeAttacked(host)) {
				appendTask(new GoToMovingLocation(
					hostId,
					new ReturnIndividualPosition(alive),
					new WithinAttackRangeOrCantAttack(hostId, alive.getId())
				));

				appendTask(new AttackTarget(host.getId(), alive.getId().getId()));
			} else {
				getHost().speak(Util.randomOneOf("Need to wait...", "Already surrounded...", "Not enough room...", "Hold on...", "Can't get in..."), 1200L);
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
	public Attack(final IndividualIdentifier hostId, final Set<Integer> toBeAttacked) {
		this(Domain.getIndividual(hostId.getId()), transformSet(toBeAttacked, id -> {return Domain.getIndividual(id);}));
	}


	@Override
	public final String getShortDescription() {
		return "Attacking";
	}


	final Individual getAlive() {
		for (final Integer id : toBeAttacked) {
			final Individual individual = Domain.getIndividual(id);
			if (individual != null && individual.getState().health > 0f) {
				return individual;
			}
		}

		return null;
	}


	public final Set<Integer> getTargets() {
		return toBeAttacked;
	}


	public final class WithinAttackRangeOrCantAttack implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -927709499203093624L;
		private IndividualIdentifier attacker;
		private IndividualIdentifier attackee;

		/**
		 * Constructor
		 */
		public WithinAttackRangeOrCantAttack(final IndividualIdentifier attacker, final IndividualIdentifier attackee) {
			this.attacker = attacker;
			this.attackee = attackee;
		}


		@Override
		public final Boolean call() {
			final Individual atker = getIndividual(attacker.getId());
			final Individual victim = getIndividual(attackee.getId());

			boolean closeEnough = false;
			final AITask subTask = getCurrentTask();
			if (subTask instanceof GoToMovingLocation) {
				final int size = ((GoToMovingLocation) subTask).getCurrentGoToLocation().getPath().getSize();
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
	@ExecutedBy(ReevaluateAttackExecutor.class)
	public final class ReevaluateAttack extends AITask {
		private static final long serialVersionUID = 7740671063229381512L;

		/**
		 * Constructor
		 */
		public ReevaluateAttack(final IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public final String getShortDescription() {
			return "";
		}


		public Attack getParent() {
			return Attack.this;
		}
	}


	/**
	 * AITask representing the actual attack
	 *
	 * @author Matt
	 */
	@ExecutedBy(AttackTargetExecutor.class)
	public final class AttackTarget extends AITask {
		private static final long serialVersionUID = -6824012439864939617L;
		boolean complete;
		int target;

		protected AttackTarget(final IndividualIdentifier hostId, final int target) {
			super(hostId);
			this.target = target;
		}


		@Override
		public final String getShortDescription() {
			return "Attacking";
		}


		public final Attack getParent() {
			return Attack.this;
		}
	}


	public static final class AttackTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = 6935537151752635203L;
		private final List<Integer> victimIds;
		private final int attackerId;
		private String attackerName;

		public AttackTaskGenerator(final int attackerId, final List<Integer> victimIds) {
			this.attackerId = attackerId;
			this.victimIds = victimIds;

			this.attackerName = Domain.getIndividual(attackerId).getId().getSimpleName();
		}

		@Override
		public final AITask apply(final Object input) {
			final List<Integer> validVictims = Lists.newLinkedList();
			for (final int id : victimIds) {
				final Individual victim = Domain.getIndividual(id);
				if (victim.isAlive()) {
					validVictims.add(id);
				}
			}

			if (validVictims.isEmpty()) {
				return null;
			} else if (validVictims.size() > 1) {
				final Attack attackTask = new Attack(Domain.getIndividual(attackerId), Domain.getIndividual(validVictims.get(0)));
				final ArrayList<Integer> validVictimsCopy = Lists.newArrayList(validVictims);
				validVictimsCopy.remove(0);

				for (final int i : validVictimsCopy) {
					attackTask.appendTask(new Attack(Domain.getIndividual(attackerId), Domain.getIndividual(i)));
				}

				return attackTask;
			} else {
				return new Attack(Domain.getIndividual(attackerId), Domain.getIndividual(validVictims.get(0)));
			}
		}

		@Override
		public final String getDailyRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public final String getEntityVisibleRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public final String getIndividualConditionRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public final String getStimulusDrivenRoutineDetailedDescription() {
			return getDescription();
		}

		private String getDescription() {
			return attackerName + " attacks selected individuals";
		}

		@Override
		public final boolean valid() {
			final List<Integer> validVictims = Lists.newLinkedList();
			for (final int id : victimIds) {
				final Individual victim = Domain.getIndividual(id);
				if (victim.isAlive()) {
					validVictims.add(id);
				}
			}

			return !validVictims.isEmpty();
		}

		@Override
		public void render() {
			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);

			final List<Integer> validVictims = Lists.newLinkedList();
			for (final int id : victimIds) {
				final Individual victim = Domain.getIndividual(id);
				if (victim.isAlive()) {
					validVictims.add(id);
				}
			}

			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(Color.RED);
			Gdx.gl20.glLineWidth(2f);
			for (final int i : validVictims) {
				final Individual individual = Domain.getIndividual(i);
				final Vector2 position = individual.getState().position;

				userInterface.getShapeRenderer().rect(
					worldToScreenX(position.x) - individual.getWidth()/2,
					worldToScreenY(position.y),
					individual.getWidth(),
					individual.getHeight()
				);

			}

			userInterface.getShapeRenderer().setColor(Color.GREEN);
			final Individual attacker = Domain.getIndividual(attackerId);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);

			userInterface.getShapeRenderer().end();
		}
	}


	public static final class AttackVisibleIndividualTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = 6935537151752635203L;
		private final VisibleIndividualFuture victimId;
		private final int attackerId;
		private String attackerName, victimName;

		public AttackVisibleIndividualTaskGenerator(final int attackerId, final VisibleIndividualFuture victimId, final String overriddenVictimName) {
			this.attackerId = attackerId;
			this.victimId = victimId;

			this.attackerName = Domain.getIndividual(attackerId).getId().getSimpleName();
			this.victimName = overriddenVictimName;
		}

		@Override
		public final AITask apply(final Object input) {
			if (!Domain.getIndividual(victimId.call()).isAlive()) {
				return null;
			}

			if (Domain.getIndividual(victimId.call()) == null) {
				return null;
			}

			return new Attack(Domain.getIndividual(attackerId), Domain.getIndividual(victimId.call()));
		}

		@Override
		public final String getDailyRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public final String getEntityVisibleRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public final String getIndividualConditionRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public final String getStimulusDrivenRoutineDetailedDescription() {
			return getDescription();
		}

		private String getDescription() {
			return attackerName + " attacks " + victimName;
		}

		@Override
		public final boolean valid() {
			return true;
		}

		@Override
		public void render() {
			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(Color.GREEN);
			Gdx.gl20.glLineWidth(2f);
			final Individual attacker = Domain.getIndividual(attackerId);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);

			userInterface.getShapeRenderer().end();
		}
	}


	public static final class ReturnVictimId implements SerializableFunction<Integer> {
		private static final long serialVersionUID = 5647294340775051321L;
		private int id;

		public ReturnVictimId(final int id) {
			this.id = id;
		}

		@Override
		public final Integer call() {
			return id;
		}
	}
}