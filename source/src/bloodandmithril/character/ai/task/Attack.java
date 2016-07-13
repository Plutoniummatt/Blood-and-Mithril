package bloodandmithril.character.ai.task;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static bloodandmithril.util.Util.transformSet;
import static bloodandmithril.world.Domain.getIndividual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AIProcessor.ReturnIndividualPosition;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.VisibleIndividualFuture;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.combat.CombatService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.cursorboundtask.ChooseMultipleEntityCursorBoundTask;
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
public final class Attack extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = 1106295624210596573L;

	private final Set<Integer> toBeAttacked = Sets.newHashSet();

	@Inject private transient GameClientStateTracker gameClientStateTracker;
	@Inject private transient Controls controls;

	@Inject
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
	public Attack(final IndividualIdentifier hostId, final Set<Integer> toBeAttacked) {
		this(Domain.getIndividual(hostId.getId()), transformSet(toBeAttacked, id -> {return Domain.getIndividual(id);}));
	}


	@Override
	public final String getShortDescription() {
		return "Attacking";
	}


	@Override
	public final boolean isComplete() {
		return getAlive() == null;
	}


	@Override
	protected final void internalExecute(final float delta) {
		if (getHost().isWalking()) {
			getHost().setWalking(false);
		}

		super.executeTask(delta);
	}


	@Override
	public final boolean uponCompletion() {
		final Individual host = Domain.getIndividual(hostId.getId());
		host.sendCommand(controls.moveRight.keyCode, false);
		host.sendCommand(controls.moveLeft.keyCode, false);
		if (!getHost().isWalking()) {
			getHost().setWalking(true);
		}
		return false;
	}


	private final Individual getAlive() {
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


		@Override
		public final boolean isComplete() {
			return true;
		}


		@Override
		public final boolean uponCompletion() {
			appendTask(new Attack(hostId, toBeAttacked));
			return false;
		}


		@Override
		protected final void internalExecute(final float delta) {
		}
	}


	/**
	 * AITask representing the actual attack
	 *
	 * @author Matt
	 */
	public final class AttackTarget extends AITask {
		private static final long serialVersionUID = -6824012439864939617L;
		private boolean complete;
		private int target;

		protected AttackTarget(final IndividualIdentifier hostId, final int target) {
			super(hostId);
			this.target = target;
		}


		@Override
		public final String getShortDescription() {
			return "Attacking";
		}


		@Override
		public final boolean isComplete() {
			return complete;
		}


		@Override
		public final boolean uponCompletion() {
			appendTask(new Attack(hostId, toBeAttacked));
			return false;
		}


		@Override
		protected final void internalExecute(final float delta) {
			getHost().setCombatStance(true);
			final Individual alive = getAlive();
			if (alive == null) {
				return;
			}

			final Individual attacker = Domain.getIndividual(hostId.getId());
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
			final List<Integer> validVictims = Lists.newLinkedList();
			for (final int id : victimIds) {
				final Individual victim = Domain.getIndividual(id);
				if (victim.isAlive()) {
					validVictims.add(id);
				}
			}

			UserInterface.shapeRenderer.begin(ShapeType.Line);
			UserInterface.shapeRenderer.setColor(Color.RED);
			Gdx.gl20.glLineWidth(2f);
			for (final int i : validVictims) {
				final Individual individual = Domain.getIndividual(i);
				final Vector2 position = individual.getState().position;

				UserInterface.shapeRenderer.rect(
					worldToScreenX(position.x) - individual.getWidth()/2,
					worldToScreenY(position.y),
					individual.getWidth(),
					individual.getHeight()
				);

			}

			UserInterface.shapeRenderer.setColor(Color.GREEN);
			final Individual attacker = Domain.getIndividual(attackerId);
			UserInterface.shapeRenderer.rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);

			UserInterface.shapeRenderer.end();
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
			UserInterface.shapeRenderer.begin(ShapeType.Line);
			UserInterface.shapeRenderer.setColor(Color.GREEN);
			Gdx.gl20.glLineWidth(2f);
			final Individual attacker = Domain.getIndividual(attackerId);
			UserInterface.shapeRenderer.rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);

			UserInterface.shapeRenderer.end();
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


	private final MenuItem chooseTargetMenuItem(final Individual host, final Routine routine) {
		return new MenuItem(
			"Choose targets",
			() -> {
				Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
					new ChooseMultipleEntityCursorBoundTask<Individual, Integer>(true, Individual.class) {
						@Override
						public boolean canAdd(final Individual f) {
							return f.isAlive() && f.getId().getId() != host.getId().getId();
						}
						@Override
						public Integer transform(final Individual f) {
							return f.getId().getId();
						}
						@Override
						public void renderUIGuide(final Graphics graphics) {
							UserInterface.shapeRenderer.begin(ShapeType.Line);
							UserInterface.shapeRenderer.setColor(Color.RED);
							Gdx.gl20.glLineWidth(2f);
							for (final int i : entities) {
								final Individual individual = Domain.getIndividual(i);
								final Vector2 position = individual.getState().position;

								UserInterface.shapeRenderer.rect(
									worldToScreenX(position.x) - individual.getWidth()/2,
									worldToScreenY(position.y),
									individual.getWidth(),
									individual.getHeight()
								);

							}
							UserInterface.shapeRenderer.end();
						}
						@Override
						public boolean executionConditionMet() {
							final Collection<Individual> nearbyEntities = gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Individual.class, getMouseWorldX(), getMouseWorldY());
							for (final Individual indi : nearbyEntities) {
								if (indi.isMouseOver()) {
									return true;
								}
							}
							return false;
						}
						@Override
						public String getShortDescription() {
							return "Choose targets (Press enter to finalise)";
						}
						@Override
						public void keyPressed(final int keyCode) {
							if (keyCode == Keys.ENTER) {
								routine.setAiTaskGenerator(new AttackTaskGenerator(host.getId().getId(), entities));
								Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(null);
							}
						}
					}
				);
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			null
		);
	}


	@Override
	public final ContextMenu getDailyRoutineContextMenu(final Individual host, final DailyRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseTargetMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public final ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseTargetMenuItem(host, routine)
		);

		final EntityVisible identificationFunction = routine.getIdentificationFunction();
		if (Individual.class.isAssignableFrom(identificationFunction.getEntity().a)) {
			menu.addFirst(
				new MenuItem(
					"Visible individual",
					() -> {
						routine.setAiTaskGenerator(new AttackVisibleIndividualTaskGenerator(host.getId().getId(), new EntityVisibleRoutine.VisibleIndividualFuture(routine), "visible individual"));
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
	public final ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseTargetMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public final ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseTargetMenuItem(host, routine)
		);

		return menu;
	}
}