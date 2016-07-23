package bloodandmithril.character.ai.task.gotolocation;

import static bloodandmithril.character.ai.task.gotolocation.GoToLocation.goTo;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.Controls;
import bloodandmithril.core.Copyright;
import bloodandmithril.world.Domain;

/**
 * Executes {@link GoToMovingLocation}
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class GoToMovingLocationExecutor implements AITaskExecutor {
	
	@Inject private GoToLocationExecutor goToLocationExecutor;
	@Inject private Controls controls;

	@Override
	public void execute(AITask aiTask, float delta) {
		GoToMovingLocation task = (GoToMovingLocation) aiTask;
		
		goToLocationExecutor.execute(task.currentGoToLocation, delta);
		
		if (task.repathCondition == null ? goToLocationExecutor.isComplete(task.getCurrentGoToLocation()) : task.repathCondition.call()) {
			final Individual host = Domain.getIndividual(task.getHostId().getId());
			task.currentGoToLocation = goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(task.destination.call()),
				false,
				150f,
				true
			);
		}		
	}
	

	@Override
	public boolean isComplete(AITask aiTask) {
		GoToMovingLocation task = (GoToMovingLocation) aiTask;
		
		if (task.terminationCondition != null) {
			return task.terminationCondition.call();
		}

		return Domain.getIndividual(task.getHostId().getId()).getDistanceFrom(task.destination.call()) < task.tolerance;
	}

	
	@Override
	public boolean uponCompletion(AITask aiTask) {
		final Individual host = Domain.getIndividual(aiTask.getHostId().getId());

		host.sendCommand(controls.moveRight.keyCode, false);
		host.sendCommand(controls.moveLeft.keyCode, false);
		host.sendCommand(controls.walk.keyCode, host.isWalking());

		return false;
	}
}