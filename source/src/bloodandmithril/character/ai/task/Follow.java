package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

public class Follow extends CompositeAITask {
	private static final long serialVersionUID = 6587958819221672725L;

	private SerializableFunction<Boolean> terminationCondition;
	private Individual followee;
	private int distance;

	/**
	 * Constructor, the int distance is the maximum number of waypoints to stay at
	 */
	public Follow(final Individual follower, Individual followee, final int distance, SerializableFunction<Boolean> terminationCondition) {
		super(follower.getId(), "Following");
		this.followee = followee;
		this.distance = distance;
		this.terminationCondition = terminationCondition;

		WithinNumberOfWaypointsFunction condition = new WithinNumberOfWaypointsFunction();
		RepathCondition repathCondition = new RepathCondition();

		appendTask(
			new GoToMovingLocation(
				follower.getId(),
				followee.getState().position,
				condition,
				repathCondition
			)
		);

		if (!condition.call()) {
			appendTask(
				new Wait(follower, Util.getRandom().nextFloat() * 2f)
			);
		}
	}


	/**
	 * @see bloodandmithril.character.ai.AITask#isComplete()
	 */
	@Override
	public boolean isComplete() {
		if (terminationCondition != null) {
			return terminationCondition.call();
		} else {
			return super.isComplete();
		}
	}



	/**
	 * @see bloodandmithril.character.ai.AITask#execute()
	 */
	@Override
	public void execute(float delta) {
		if (getCurrentTask() != null) {
			super.execute(delta);
		}
	}


	@Override
	public boolean uponCompletion() {
		if (terminationCondition != null && terminationCondition.call()) {
			return false;
		} else {
			Individual follower = Domain.getIndividuals().get(hostId.getId());
			follower.getAI().setCurrentTask(new Follow(follower, followee, distance, terminationCondition));
			return true;
		}
	}


	public class WithinNumberOfWaypointsFunction implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -4758106924647625767L;

		@Override
		public Boolean call() {
			AITask currentTask = getCurrentTask();
			if (currentTask instanceof GoToMovingLocation) {
				return ((GoToMovingLocation)currentTask).getCurrentGoToLocation().getPath().getSize() <= distance;
			}

			return true;
		}
	}


	public class RepathCondition implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -2157220355764032631L;

		@Override
		public Boolean call() {
			AITask currentTask = getCurrentTask();
			if (currentTask instanceof GoToMovingLocation) {
				return ((GoToMovingLocation)currentTask).getCurrentGoToLocation().getPath().getSize() == distance + 1;
			}

			return true;
		}
	}
}