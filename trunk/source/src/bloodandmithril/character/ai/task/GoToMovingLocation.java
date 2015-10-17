package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.core.BloodAndMithrilClient.getKeyMappings;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldX;
import static bloodandmithril.core.BloodAndMithrilClient.getMouseWorldY;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenX;
import static bloodandmithril.core.BloodAndMithrilClient.worldToScreenY;
import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
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
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
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
public class GoToMovingLocation extends AITask implements RoutineTask {
	private static final long serialVersionUID = 3940840091194740269L;

	/** The changing destination */
	private final Vector2 destination;
	private final float tolerance;
	private GoToLocation currentGoToLocation;
	private SerializableFunction<Boolean> terminationCondition;

	private SerializableFunction<Boolean> repathCondition;

	@Inject
	GoToMovingLocation() {
		super(null);
		this.tolerance = 0;
		this.destination = null;
	}

	/**
	 * Constructor
	 */
	protected GoToMovingLocation(IndividualIdentifier hostId, Vector2 destination, float tolerance) {
		super(hostId);
		this.destination = destination;
		this.tolerance = tolerance;

		Individual host = Domain.getIndividual(hostId.getId());
		this.currentGoToLocation = goTo(
			host,
			host.getState().position.cpy(),
			new WayPoint(destination),
			false,
			150f,
			true
		);
	}


	/**
	 * Constructor
	 */
	protected GoToMovingLocation(IndividualIdentifier hostId, Vector2 destination, SerializableFunction<Boolean> terminationCondition) {
		super(hostId);
		this.destination = destination;
		this.terminationCondition = terminationCondition;
		this.tolerance = -1f;

		Individual host = Domain.getIndividual(hostId.getId());
		this.currentGoToLocation = goTo(
			host,
			host.getState().position.cpy(),
			new WayPoint(destination),
			false,
			150f,
			true
		);
	}


	/**
	 * Constructor
	 */
	protected GoToMovingLocation(IndividualIdentifier hostId, Vector2 destination, SerializableFunction<Boolean> terminationCondition, SerializableFunction<Boolean> repathCondition) {
		super(hostId);
		this.destination = destination;
		this.terminationCondition = terminationCondition;
		this.repathCondition = repathCondition;
		this.tolerance = -1f;

		Individual host = Domain.getIndividual(hostId.getId());
		this.currentGoToLocation = goTo(
			host,
			host.getState().position.cpy(),
			new WayPoint(destination),
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

		return Domain.getIndividual(hostId.getId()).getDistanceFrom(destination) < tolerance;
	}


	/**
	 * See {@link Path#isDirectlyAboveNext(Vector2)}
	 */
	public boolean isAboveNext(Vector2 location) {
		return this.currentGoToLocation.getPath().isDirectlyAboveNext(location);
	}


	@Override
	public boolean uponCompletion() {
		Individual host = Domain.getIndividual(hostId.getId());

		host.sendCommand(getKeyMappings().moveRight.keyCode, false);
		host.sendCommand(getKeyMappings().moveLeft.keyCode, false);
		host.sendCommand(getKeyMappings().walk.keyCode, host.isWalking());

		return false;
	}


	@Override
	public void execute(float delta) {
		getCurrentGoToLocation().execute(delta);

		if (repathCondition == null ? getCurrentGoToLocation().isComplete() : repathCondition.call()) {
			Individual host = Domain.getIndividual(hostId.getId());
			this.currentGoToLocation = goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(destination),
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

		public GoToMovingLocationTaskGenerator(float x, float y, int hostId) {
			this.x = x;
			this.y = y;
			this.hostId = hostId;
		}

		@Override
		public AITask apply(Object input) {
			Individual individual = Domain.getIndividual(hostId);
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
	}


	private MenuItem chooseLocationMenuItem(Individual host, Routine routine) {
		return new MenuItem(
				"Choose location",
				() -> {
					JITTask task = new JITTask() {
						@Override
						public void execute(Object... args) {
							Vector2 coords;
							try {
								coords = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);
							} catch (NoTileFoundException e) {
								return;
							}

							float x = getMouseWorldX();
							float y = coords.y;

							routine.setAiTaskGenerator(
								new GoToMovingLocationTaskGenerator(x, y, host.getId().getId())
							);
						}
					};

					BloodAndMithrilClient.setCursorBoundTask(new CursorBoundTask(task, true) {
						@Override
						public void renderUIGuide() {
							try {
								Vector2 coords = Domain.getActiveWorld().getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getMouseWorldX(), getMouseWorldY(), true);

								float x = worldToScreenX(getMouseWorldX());
								float y = worldToScreenY(coords.y);

								gl.glEnable(GL_BLEND);
								gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
								getGraphics().getSpriteBatch().begin();
								getGraphics().getSpriteBatch().setColor(executionConditionMet() ? Color.GREEN : Color.RED);
								getGraphics().getSpriteBatch().draw(UserInterface.currentArrow, x - 5, y);
								getGraphics().getSpriteBatch().end();
								gl.glDisable(GL_BLEND);
							} catch (NoTileFoundException e) {}
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
						public void keyPressed(int keyCode) {
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
	public ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine) {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseLocationMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine routine) {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseLocationMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine) {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseLocationMenuItem(host, routine)
		);

		return menu;
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine) {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			chooseLocationMenuItem(host, routine)
		);

		return menu;
	}
}