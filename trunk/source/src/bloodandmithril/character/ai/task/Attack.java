package bloodandmithril.character.ai.task;

import static bloodandmithril.core.BloodAndMithrilClient.getKeyMappings;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.util.Util.transformSet;
import static bloodandmithril.world.Domain.getIndividual;

import java.util.Set;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.combat.CombatService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

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
public class Attack extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = 1106295624210596573L;

	private Set<Integer> toBeAttacked = Sets.newHashSet();

	@Inject
	Attack() {
		super(null, "");
	}

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
	public String getShortDescription() {
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
		public String getShortDescription() {
			return "";
		}


		@Override
		public boolean isComplete() {
			return true;
		}


		@Override
		public boolean uponCompletion() {
			appendTask(new Attack(hostId, toBeAttacked));
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
		public String getShortDescription() {
			return "Attacking";
		}


		@Override
		public boolean isComplete() {
			return complete;
		}


		@Override
		public boolean uponCompletion() {
			appendTask(new Attack(hostId, toBeAttacked));
			return false;
		}


		@Override
		public void execute(float delta) {
			getHost().setCombatStance(true);
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


	public static class AttackTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = 6935537151752635203L;
		private SerializableFunction<Integer> victimId;
		private String attackerName, victimName;
		private int attackerId;

		public AttackTaskGenerator(int attackerId, SerializableFunction<Integer> victimId, String overriddenVictimName) {
			this.attackerId = attackerId;
			this.victimId = victimId;

			this.attackerName = Domain.getIndividual(attackerId).getId().getSimpleName();

			if (overriddenVictimName != null) {
				this.victimName = overriddenVictimName;
			}
		}

		@Override
		public AITask apply(Object input) {
			if (!Domain.getIndividual(victimId.call()).isAlive()) {
				return null;
			}

			if (Domain.getIndividual(victimId.call()) == null) {
				return null;
			}

			return new Attack(Domain.getIndividual(attackerId), Domain.getIndividual(victimId.call()));
		}

		@Override
		public String getDailyRoutineDetailedDescription() {
			return attackerName + " attacks " + victimName;
		}

		@Override
		public String getEntityVisibleRoutineDetailedDescription() {
			return attackerName + " attacks " + victimName;
		}

		@Override
		public String getIndividualConditionRoutineDetailedDescription() {
			return attackerName + " attacks " + victimName;
		}

		@Override
		public String getStimulusDrivenRoutineDetailedDescription() {
			return attackerName + " attacks " + victimName;
		}
	}


	public static class ReturnVictimId implements SerializableFunction<Integer> {
		private static final long serialVersionUID = 5647294340775051321L;
		private int id;

		public ReturnVictimId(int id) {
			this.id = id;
		}

		@Override
		public Integer call() {
			return id;
		}
	}


	private MenuItem chooseTargetMenuItem(Individual host, Routine routine, ContextMenu toChooseFrom) {
		return new MenuItem(
			"Choose target",
			() -> {
				JITTask task = new JITTask() {
					@Override
					public void execute(Object... args) {
						if (Domain.getActiveWorld() != null) {
							for (int indiKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Individual.class, getMouseWorldX(), getMouseWorldY())) {
								Individual indi = Domain.getIndividual(indiKey);
								if (indi.isMouseOver()) {
									toChooseFrom.addMenuItem(
										new MenuItem(
											"Attack " + indi.getId().getSimpleName(),
											() -> {
												routine.setAiTaskGenerator(new AttackTaskGenerator(host.getId().getId(), new ReturnVictimId(indi.getId().getId()), indi.getId().getSimpleName()));
											},
											Color.ORANGE,
											Color.GREEN,
											Color.GRAY,
											null
										)
									);
								}
							}
						}

						UserInterface.contextMenus.clear();
						toChooseFrom.x = getMouseScreenX();
						toChooseFrom.y = getMouseScreenY();
						UserInterface.contextMenus.add(toChooseFrom);
					}
				};

				BloodAndMithrilClient.setCursorBoundTask(new CursorBoundTask(task, true) {
					@Override
					public void renderUIGuide() {
					}

					@Override
					public String getShortDescription() {
						return "Choose target";
					}

					@Override
					public CursorBoundTask getImmediateTask() {
						return null;
					}

					@Override
					public boolean executionConditionMet() {
						if (Domain.getActiveWorld() != null) {
							for (int indiKey : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Individual.class, getMouseWorldX(), getMouseWorldY())) {
								Individual indi = Domain.getIndividual(indiKey);
								if (indi.isMouseOver()) {
									return true;
								}
							}
						}

						return false;
					}

					@Override
					public boolean canCancel() {
						return true;
					}
				});
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			null
		);
	}


	public static class VisibleIndividualFuture implements SerializableFunction<Integer> {
		private static final long serialVersionUID = 3527567985423803956L;
		private EntityVisibleRoutine routine;

		public VisibleIndividualFuture(EntityVisibleRoutine routine) {
			this.routine = routine;
		}

		@Override
		public Integer call() {
			Visible visibleEntity = routine.getVisibleEntity();
			if (visibleEntity instanceof Individual) {
				return ((Individual) visibleEntity).getId().getId();
			}

			throw new RuntimeException();
		}
	}


	@Override
	public ContextMenu getDailyRoutineContextMenu(Individual host, final DailyRoutine routine) {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseTargetMenuItem(host, routine, toChooseFrom)
		);

		return menu;
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine routine) {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseTargetMenuItem(host, routine, toChooseFrom)
		);

		final EntityVisible identificationFunction = routine.getIdentificationFunction();
		if (Individual.class.isAssignableFrom(identificationFunction.getEntity().a)) {
			menu.addFirst(
				new MenuItem(
					"Visible individual",
					() -> {
						routine.setAiTaskGenerator(new AttackTaskGenerator(host.getId().getId(), new VisibleIndividualFuture(routine), "visible individual"));
					},
					Color.MAGENTA,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);
		}

		return menu;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine) {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseTargetMenuItem(host, routine, toChooseFrom)
		);

		return menu;
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine) {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseTargetMenuItem(host, routine, toChooseFrom)
		);

		return menu;
	}
}