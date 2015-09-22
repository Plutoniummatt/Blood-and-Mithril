package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.google.inject.Inject;

@Copyright("Matthew Peck 2014")
@Name(name = "Follow")
public class Follow extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = 6587958819221672725L;

	private SerializableFunction<Boolean> terminationCondition;
	private Individual followee;
	private int distance;

	@Inject
	Follow() {
		super(null, "");
	}

	/**
	 * Constructor, the int distance is the maximum number of waypoints to stay at
	 */
	public Follow(final Individual follower, Individual followee, final int distance, SerializableFunction<Boolean> terminationCondition) {
		super(follower.getId(), "Following");
		this.followee = followee;
		this.distance = distance;
		this.terminationCondition = terminationCondition;

		WithinNumberOfWaypointsFunction termCondition = new WithinNumberOfWaypointsFunction();
		RepathCondition repathCondition = new RepathCondition();

		appendTask(
			new GoToMovingLocation(
				follower.getId(),
				followee.getState().position,
				termCondition,
				repathCondition
			)
		);

		if (!termCondition.call()) {
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
		if (!followee.isAlive()) {
			return true;
		}

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
			Individual follower = Domain.getIndividual(hostId.getId());
			if (follower == null) {
				return false;
			}
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


	@Override
	public String getDetailedDescription() {
		return getHost().getId().getSimpleName() + " follows " + followee.getId().getSimpleName();
	}


	@Override
	public ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine<? extends Visible> routine) {
		return null;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine) {
		return null;
	}
}