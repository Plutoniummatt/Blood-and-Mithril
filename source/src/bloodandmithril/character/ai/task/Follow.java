package bloodandmithril.character.ai.task;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AIProcessor.ReturnIndividualPosition;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.ai.task.Attack.ReturnVictimId;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

@Copyright("Matthew Peck 2014")
@Name(name = "Follow")
public class Follow extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = 6587958819221672725L;

	private SerializableFunction<Boolean> terminationCondition;
	private Individual followee;
	private int distance;

	@Inject
	private GameClientStateTracker gameClientStateTracker;
	@Inject
	private UserInterface userInterface;

	@Inject
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


	@Override
	public boolean uponCompletion() {
		if (terminationCondition != null && terminationCondition.call()) {
			return false;
		} else {
			final Individual follower = Domain.getIndividual(hostId.getId());
			if (follower == null) {
				return false;
			}
			appendTask(new Follow(follower, followee, distance, terminationCondition));
			return true;
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
			UserInterface.shapeRenderer.begin(ShapeType.Line);
			Gdx.gl20.glLineWidth(2f);
			UserInterface.shapeRenderer.setColor(Color.GREEN);
			final Individual attacker = Domain.getIndividual(followerId);
			UserInterface.shapeRenderer.rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);

			UserInterface.shapeRenderer.setColor(Color.RED);
			final Individual followee = Domain.getIndividual(followeeId.call());
			UserInterface.shapeRenderer.rect(
				worldToScreenX(followee.getState().position.x) - followee.getWidth()/2,
				worldToScreenY(followee.getState().position.y),
				followee.getWidth(),
				followee.getHeight()
			);
			UserInterface.shapeRenderer.end();
		}
	}


	private MenuItem chooseFolloweeMenuItem(final Individual host, final Routine routine, final ContextMenu toChooseFrom) {
		return new MenuItem(
			"Choose individual to follow",
			() -> {
				final JITTask task = new JITTask() {
					@Override
					public void execute(final Object... args) {
						if (gameClientStateTracker.getActiveWorld() != null) {
							for (final int indiKey : gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
								final Individual indi = Domain.getIndividual(indiKey);
								if (indi.isMouseOver()) {
									toChooseFrom.addMenuItem(
										new MenuItem(
											"Follow " + indi.getId().getSimpleName(),
											() -> {
												routine.setAiTaskGenerator(new FollowTaskGenerator(host.getId().getId(), new ReturnVictimId(indi.getId().getId()), indi.getId().getSimpleName()));
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

						userInterface.contextMenus.clear();
						toChooseFrom.x = getMouseScreenX();
						toChooseFrom.y = getMouseScreenY();
						userInterface.contextMenus.add(toChooseFrom);
					}
				};

				Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(new CursorBoundTask(task, true) {
					@Override
					public void renderUIGuide(final Graphics graphics) {
					}

					@Override
					public String getShortDescription() {
						return "Choose individual to follow";
					}

					@Override
					public CursorBoundTask getImmediateTask() {
						return null;
					}

					@Override
					public boolean executionConditionMet() {
						if (gameClientStateTracker.getActiveWorld() != null) {
							for (final int indiKey : gameClientStateTracker.getActiveWorld().getPositionalIndexMap().getNearbyEntityIds(Individual.class, getMouseWorldX(), getMouseWorldY())) {
								final Individual indi = Domain.getIndividual(indiKey);
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

					@Override
					public void keyPressed(final int keyCode) {
					}
				});
			},
			Color.ORANGE,
			Color.GREEN,
			Color.GRAY,
			null
		);
	}


	@Override
	public ContextMenu getDailyRoutineContextMenu(final Individual host, final DailyRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		final ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseFolloweeMenuItem(host, routine, toChooseFrom)
		);

		return menu;
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		final ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseFolloweeMenuItem(host, routine, toChooseFrom)
		);

		final EntityVisible identificationFunction = routine.getIdentificationFunction();
		if (Individual.class.isAssignableFrom(identificationFunction.getEntity().a)) {
			menu.addFirst(
				new MenuItem(
					"Visible individual",
					() -> {
						routine.setAiTaskGenerator(new FollowTaskGenerator(host.getId().getId(), new EntityVisibleRoutine.VisibleIndividualFuture(routine), "visible individual"));
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
	public ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		final ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseFolloweeMenuItem(host, routine, toChooseFrom)
		);

		return menu;
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		final ContextMenu toChooseFrom = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseFolloweeMenuItem(host, routine, toChooseFrom)
		);

		return menu;
	}
}