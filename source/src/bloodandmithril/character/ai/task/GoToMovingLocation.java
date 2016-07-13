package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.NextWaypointProvider;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.pathfinding.Path;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
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
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.JITTask;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Moves a host to a moving location
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Go to location")
public class GoToMovingLocation extends AITask implements RoutineTask, NextWaypointProvider {
	private static final long serialVersionUID = 3940840091194740269L;

	/** The changing destination */
	private final SerializableFunction<Vector2> destination;
	private final float tolerance;
	private GoToLocation currentGoToLocation;
	private SerializableFunction<Boolean> terminationCondition;
	private SerializableFunction<Boolean> repathCondition;

	@Inject private transient GameClientStateTracker gameClientStateTracker;
	@Inject private transient Controls controls;

	@Inject
	GoToMovingLocation() {
		super(null);
		this.tolerance = 0;
		this.destination = null;
	}

	/**
	 * Constructor
	 */
	protected GoToMovingLocation(final IndividualIdentifier hostId, final SerializableFunction<Vector2> destination, final float tolerance) {
		super(hostId);
		this.destination = destination;
		this.tolerance = tolerance;

		final Individual host = Domain.getIndividual(hostId.getId());
		this.currentGoToLocation = goTo(
			host,
			host.getState().position.cpy(),
			new WayPoint(destination.call()),
			false,
			150f,
			true
		);
	}


	/**
	 * Constructor
	 */
	protected GoToMovingLocation(final IndividualIdentifier hostId, final SerializableFunction<Vector2> destination, final SerializableFunction<Boolean> terminationCondition) {
		super(hostId);
		this.destination = destination;
		this.terminationCondition = terminationCondition;
		this.tolerance = -1f;

		final Individual host = Domain.getIndividual(hostId.getId());
		this.currentGoToLocation = goTo(
			host,
			host.getState().position.cpy(),
			new WayPoint(destination.call()),
			false,
			150f,
			true
		);
	}


	/**
	 * Constructor
	 */
	protected GoToMovingLocation(final IndividualIdentifier hostId, final SerializableFunction<Vector2> destination, final SerializableFunction<Boolean> terminationCondition, final SerializableFunction<Boolean> repathCondition) {
		super(hostId);
		this.destination = destination;
		this.terminationCondition = terminationCondition;
		this.repathCondition = repathCondition;
		this.tolerance = -1f;

		final Individual host = Domain.getIndividual(hostId.getId());
		this.currentGoToLocation = goTo(
			host,
			host.getState().position.cpy(),
			new WayPoint(destination.call()),
			false,
			150f,
			true
		);
	}


	@Override
	public String getShortDescription() {
		return "Moving";
	}


	@Override
	public boolean isComplete() {
		if (terminationCondition != null) {
			return terminationCondition.call();
		}

		return Domain.getIndividual(hostId.getId()).getDistanceFrom(destination.call()) < tolerance;
	}


	/**
	 * See {@link Path#isDirectlyAboveNext(Vector2)}
	 */
	public boolean isAboveNext(final Vector2 location) {
		return this.currentGoToLocation.getPath().isDirectlyAboveNext(location);
	}


	@Override
	public boolean uponCompletion() {
		final Individual host = Domain.getIndividual(hostId.getId());

		host.sendCommand(controls.moveRight.keyCode, false);
		host.sendCommand(controls.moveLeft.keyCode, false);
		host.sendCommand(controls.walk.keyCode, host.isWalking());

		return false;
	}


	@Override
	protected void internalExecute(final float delta) {
		getCurrentGoToLocation().executeTask(delta);

		if (repathCondition == null ? getCurrentGoToLocation().isComplete() : repathCondition.call()) {
			final Individual host = Domain.getIndividual(hostId.getId());
			this.currentGoToLocation = goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(destination.call()),
				false,
				150f,
				true
			);
		}
	}

	public GoToLocation getCurrentGoToLocation() {
		return currentGoToLocation;
	}


	public static class GoToMovingLocationTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = -2000677075433369539L;
		private float x, y;
		private int hostId;

		public GoToMovingLocationTaskGenerator(final float x, final float y, final int hostId) {
			this.x = x;
			this.y = y;
			this.hostId = hostId;
		}

		@Override
		public AITask apply(final Object input) {
			final Individual individual = Domain.getIndividual(hostId);
			return goTo(
				individual,
				individual.getState().position.cpy(),
				new WayPoint(new Vector2(x, y)),
				false,
				150f,
				true
			);
		}
		@Override
		public String getDailyRoutineDetailedDescription() {
			return "Go to location at: " + String.format("%.1f", x) + ", " + String.format("%.1f", y);
		}
		@Override
		public String getEntityVisibleRoutineDetailedDescription() {
			return "Go to location at: " + String.format("%.1f", x) + ", " + String.format("%.1f", y);
		}
		@Override
		public String getIndividualConditionRoutineDetailedDescription() {
			return "Go to location at: " + String.format("%.1f", x) + ", " + String.format("%.1f", y);
		}
		@Override
		public String getStimulusDrivenRoutineDetailedDescription() {
			return "Go to location at: " + String.format("%.1f", x) + ", " + String.format("%.1f", y);
		}
		@Override
		public boolean valid() {
			return true;
		}
		@Override
		public void render() {
			UserInterface.shapeRenderer.begin(ShapeType.Line);
			Gdx.gl20.glLineWidth(2f);
			UserInterface.shapeRenderer.setColor(Color.GREEN);
			final Individual attacker = Domain.getIndividual(hostId);
			UserInterface.shapeRenderer.rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);

			UserInterface.shapeRenderer.setColor(Color.RED);
			UserInterface.shapeRenderer.circle(worldToScreenX(x), worldToScreenY(y), 6f);
			UserInterface.shapeRenderer.end();
		}
	}


	private MenuItem chooseLocationMenuItem(final Individual host, final Routine routine) {
		return new MenuItem(
				"Choose location",
				() -> {
					final JITTask task = new JITTask() {
						@Override
						public void execute(final Object... args) {
							Vector2 coords;
							try {
								coords = gameClientStateTracker.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);
							} catch (final NoTileFoundException e) {
								return;
							}

							final float x = getMouseWorldX();
							final float y = coords.y;

							routine.setAiTaskGenerator(
								new GoToMovingLocationTaskGenerator(x, y, host.getId().getId())
							);
						}
					};

					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(new CursorBoundTask(task, true) {
						@Override
						public void renderUIGuide(final Graphics graphics) {
							try {
								final Vector2 coords = gameClientStateTracker.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);

								final float x = worldToScreenX(getMouseWorldX());
								final float y = worldToScreenY(coords.y);

								gl.glEnable(GL_BLEND);
								gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
								graphics.getSpriteBatch().begin();
								graphics.getSpriteBatch().setColor(executionConditionMet() ? Color.GREEN : Color.RED);
								graphics.getSpriteBatch().draw(UserInterface.currentArrow, x - 5, y);
								graphics.getSpriteBatch().end();
								gl.glDisable(GL_BLEND);
							} catch (final NoTileFoundException e) {}
						}

						@Override
						public String getShortDescription() {
							return "Choose location";
						}

						@Override
						public CursorBoundTask getImmediateTask() {
							return null;
						}

						@Override
						public boolean executionConditionMet() {
							return true;
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

		menu.addMenuItem(
			chooseLocationMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(final Individual host, final EntityVisibleRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseLocationMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(final Individual host, final IndividualConditionRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseLocationMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(final Individual host, final StimulusDrivenRoutine routine) {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseLocationMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public WayPoint provideNextWaypoint() {
		return getCurrentGoToLocation().getPath().getNextPoint();
	}
}