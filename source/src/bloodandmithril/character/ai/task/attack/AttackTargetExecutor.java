package bloodandmithril.character.ai.task.attack;

import com.google.common.collect.Sets;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.attack.Attack.AttackTarget;
import bloodandmithril.character.combat.CombatService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.world.Domain;

/**
 * Executes a {@link AttackTarget}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class AttackTargetExecutor implements AITaskExecutor {

	@Override
	public void execute(final AITask aiTask, final float delta) {
		final AttackTarget task = (AttackTarget) aiTask;

		task.getHost().setCombatStance(true);
		final Individual alive = task.getParent().getAlive();
		if (alive == null) {
			return;
		}

		final Individual attacker = Domain.getIndividual(task.getHostId().getId());
		if (!alive.canBeAttacked(attacker)) {
			task.complete = true;
			return;
		} else {
			alive.addAttacker(attacker);
		}

		if (task.getParent().new WithinAttackRangeOrCantAttack(task.getHostId(), alive.getId()).call()) {
			task.complete = CombatService.attack(attacker, Sets.newHashSet(task.target));
		} else {
			task.complete = true;
		}
	}


	@Override
	public final boolean isComplete(final AITask aiTask) {
		return ((AttackTarget) aiTask).complete;
	}


	@Override
	public final boolean uponCompletion(final AITask aiTask) {
		final Attack parent = ((AttackTarget) aiTask).getParent();
		parent.appendTask(new Attack(aiTask.getHostId(), parent.getTargets()));
		return false;
	}
}