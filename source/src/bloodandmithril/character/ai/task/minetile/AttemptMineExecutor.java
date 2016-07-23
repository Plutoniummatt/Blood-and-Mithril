package bloodandmithril.character.ai.task.minetile;

import static bloodandmithril.character.combat.CombatService.getAttackPeriod;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE;
import static bloodandmithril.util.ComparisonUtil.obj;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.minetile.MineTile.AttemptMine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.core.Copyright;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * Executes {@link AttemptMine}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck")
public class AttemptMineExecutor implements AITaskExecutor {

	
	@Override
	public void execute(AITask aiTask, float delta) {
		AttemptMine task = (AttemptMine) aiTask;
		
		final Individual host = Domain.getIndividual(task.getHostId().getId());

		if (obj(host.getCurrentAction()).oneOf(ATTACK_LEFT_ONE_HANDED_WEAPON_MINE, ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE)) {
			return;
		}

		if (host.getAttackTimer() < getAttackPeriod(host)) {
			return;
		}

		if (!task.getParent().new WithinInteractionBox().call()) {
			return;
		}

		host.setAnimationTimer(0f);
		host.setAttackTimer(0f);
		if (task.getParent().tileCoordinate.x < host.getState().position.x) {
			host.setCurrentAction(Action.ATTACK_LEFT_ONE_HANDED_WEAPON_MINE);
		} else {
			host.setCurrentAction(Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_MINE);
		}		
	}
	

	@Override
	public boolean isComplete(AITask aiTask) {
		AttemptMine task = (AttemptMine) aiTask;
		
		try {
			return
				!Domain.getIndividual(task.getHostId().getId()).getInteractionBox().isWithinBox(task.getParent().tileCoordinate) ||
				Domain.getWorld(Domain.getIndividual(task.getHostId().getId()).getWorldId()).getTopography().getTile(task.getParent().tileCoordinate, true) instanceof EmptyTile;
		} catch (final NoTileFoundException e) {
			return true;
		}
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}
}