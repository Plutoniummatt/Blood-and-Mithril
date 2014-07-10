package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.util.Util;

public class Follow extends CompositeAITask {
	private static final long serialVersionUID = 6587958819221672725L;

	private final IndividualIdentifier followeeId;

	/**
	 * Constructor, the int distance is the maximum number of waypoints to stay at
	 */
	public Follow(final Individual follower, Individual followee, final int distance) {
		super(follower.getId(), "Following");
		this.followeeId = followee.getId();

		appendTask(
			new GoToMovingLocation(
				follower.getId(),
				followee.getState().position,
				() -> {
					AITask currentTask = getCurrentTask();
					if (currentTask instanceof GoToMovingLocation) {
						return ((GoToMovingLocation)currentTask).getCurrentGoToLocation().getPath().getSize() <= distance;
					}

					return true;
				}
			)
		);

		appendTask(
			new Wait(follower, Util.getRandom().nextFloat() * 2f)
		);
	}
}