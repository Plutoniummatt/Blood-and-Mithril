package bloodandmithril.character.ai.task.follow;

import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AIProcessor.ReturnIndividualPosition;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.RoutineContextMenusProvidedBy;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.gotolocation.GoToMovingLocation;
import bloodandmithril.character.ai.task.wait.Wait;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

@Copyright("Matthew Peck 2014")
@Name(name = "Follow")
@ExecutedBy(FollowExecutor.class)
@RoutineContextMenusProvidedBy(FollowRoutineContextMenuProvider.class)
public class Follow extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = 6587958819221672725L;

	SerializableFunction<Boolean> terminationCondition;
	Individual followee;
	int distance;

	@Inject
	@Deprecated
	Follow() {
		super(null, "");
	}

	/**
	 * Constructor, the int distance is the maximum number of waypoints to stay at
	 */
	public Follow(final Individual follower, final Individual followee, final int distance, final SerializableFunction<Boolean> terminationCondition) {
		super(follower.getId(), "Following");
		this.followee = followee;
		this.distance = distance;
		this.terminationCondition = terminationCondition;

		final WithinNumberOfWaypointsFunction termCondition = new WithinNumberOfWaypointsFunction();
		final RepathCondition repathCondition = new RepathCondition();

		appendTask(
			new GoToMovingLocation(
				follower.getId(),
				new ReturnIndividualPosition(followee),
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


	public class WithinNumberOfWaypointsFunction implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -4758106924647625767L;

		@Override
		public Boolean call() {
			final AITask currentTask = getCurrentTask();
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
			final AITask currentTask = getCurrentTask();
			if (currentTask instanceof GoToMovingLocation) {
				return ((GoToMovingLocation)currentTask).getCurrentGoToLocation().getPath().getSize() == distance + 1;
			}

			return true;
		}
	}


	public static class FollowTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = 4507750993455699310L;
		private SerializableFunction<Integer> followeeId;
		private String followerName, followeeName;
		private int followerId;

		public FollowTaskGenerator(final int followerId, final SerializableFunction<Integer> followeeId, final String overriddenFolloweeName) {
			this.followerId = followerId;
			this.followeeId = followeeId;

			this.followerName = Domain.getIndividual(followerId).getId().getSimpleName();

			if (overriddenFolloweeName != null) {
				this.followeeName = overriddenFolloweeName;
			}
		}
		@Override
		public AITask apply(final Object input) {
			if (!Domain.getIndividual(followeeId.call()).isAlive()) {
				return null;
			}

			if (Domain.getIndividual(followeeId.call()) == null) {
				return null;
			}

			return new Follow(Domain.getIndividual(followerId), Domain.getIndividual(followeeId.call()), 8, null);
		}
		@Override
		public String getDailyRoutineDetailedDescription() {
			return followerName + " follows " + followeeName;
		}
		@Override
		public String getEntityVisibleRoutineDetailedDescription() {
			return followerName + " follows " + followeeName;
		}
		@Override
		public String getIndividualConditionRoutineDetailedDescription() {
			return followerName + " follows " + followeeName;
		}
		@Override
		public String getStimulusDrivenRoutineDetailedDescription() {
			return followerName + " follows " + followeeName;
		}
		@Override
		public boolean valid() {
			return Domain.getIndividual(followeeId.call()).isAlive() && Domain.getIndividual(followerId).isAlive();
		}
		@Override
		public void render() {
			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			Gdx.gl20.glLineWidth(2f);
			userInterface.getShapeRenderer().setColor(Color.GREEN);
			final Individual attacker = Domain.getIndividual(followerId);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);

			userInterface.getShapeRenderer().setColor(Color.RED);
			final Individual followee = Domain.getIndividual(followeeId.call());
			userInterface.getShapeRenderer().rect(
				worldToScreenX(followee.getState().position.x) - followee.getWidth()/2,
				worldToScreenY(followee.getState().position.y),
				followee.getWidth(),
				followee.getHeight()
			);
			userInterface.getShapeRenderer().end();
		}
	}
}