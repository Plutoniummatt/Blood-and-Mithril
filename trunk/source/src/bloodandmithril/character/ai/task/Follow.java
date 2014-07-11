package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

public class Follow extends CompositeAITask {
	private static final long serialVersionUID = 6587958819221672725L;
	private Individual followee;
	private int distance;

	/**
	 * Constructor, the int distance is the maximum number of waypoints to stay at
	 */
	public Follow(final Individual follower, Individual followee, final int distance) {
		super(follower.getId(), "Following");
		this.followee = followee;
		this.distance = distance;

		SerializableFunction<Boolean> function = () -> {
			AITask currentTask = getCurrentTask();
			if (currentTask instanceof GoToMovingLocation) {
				return ((GoToMovingLocation)currentTask).getCurrentGoToLocation().getPath().getSize() <= distance;
			}

			return true;
		};
		
		SerializableFunction<Boolean> repathFunction = () -> {
			AITask currentTask = getCurrentTask();
			if (currentTask instanceof GoToMovingLocation) {
				return ((GoToMovingLocation)currentTask).getCurrentGoToLocation().getPath().getSize() == distance + 1;
			}
			
			return true;
		};
		
		appendTask(
			new GoToMovingLocation(
				follower.getId(),
				followee.getState().position,
				function,
				repathFunction,
				true
			)
		);

		if (!function.call()) {
			appendTask(
				new Wait(follower, Util.getRandom().nextFloat() * 2f)
			);
		}
	}
	
	
	@Override
	public boolean uponCompletion() {
		Individual follower = Domain.getIndividuals().get(hostId.getId());
		follower.getAI().setCurrentTask(new Follow(follower, followee, distance));
		return true;
	}
}