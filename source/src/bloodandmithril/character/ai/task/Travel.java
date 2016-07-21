package bloodandmithril.character.ai.task;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.Speech;
import bloodandmithril.character.ai.AIProcessor.JitGoToLocation;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.NextWaypointProvider;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;

/**
 * A {@link Travel} task is an ordered series of {@link GoToLocation}s and {@link Jump}s that get executed sequentially
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Travel extends CompositeAITask implements NextWaypointProvider {
	private static final long serialVersionUID = -1118542666642761349L;

	@Inject private transient UserInterface userInterface;

	/**
	 * Constructor
	 */
	public Travel(final IndividualIdentifier hostId) {
		super(hostId, "Travelling");
	}


	/**
	 * Adds a {@link GoToLocation} task
	 */
	public void addGotoLocation(final JitGoToLocation goToLocation) {
		appendTask(goToLocation);
		if (Wiring.injector().getInstance(GameClientStateTracker.class).isIndividualSelected(getHost()) && Util.roll(0.3f)) {
			getHost().speak(Speech.getRandomAffirmativeSpeech(), 1000);
			getHost().playAffirmativeSound();
		}
	}


	/**
	 * Adds a {@link GoToLocation} task
	 */
	public void addJump(final Jump jump) {
		if (getCurrentTask() instanceof Jump || tasks.peekLast() instanceof Jump) {
			return;
		}
		appendTask(jump);
	}


	public Vector2 getDestination() {
		final AITask task = getCurrentTask();
		if (task instanceof GoToLocation) {
			return ((GoToLocation) task).getPath().getDestinationWayPoint().waypoint.cpy();
		} else {
			return ((Jump) task).getDestination();
		}
	}


	public Vector2 getFinalGoToLocationWaypoint() {
		final AITask peekLast = tasks.peekLast();
		if (peekLast == null) {
			final AITask currentTask = getCurrentTask();
			if (currentTask != null && currentTask instanceof JitGoToLocation) {
				return ((JitGoToLocation) currentTask).getDestination().waypoint.cpy();
			} else {
				return null;
			}
		} else {
			if (peekLast instanceof JitGoToLocation) {
				return ((JitGoToLocation) peekLast).getDestination().waypoint.cpy();
			}
			return null;
		}
	}


	/**
	 * Renders all waypoints in this {@link Travel} task
	 */
	public void renderWaypoints(final Graphics graphics) {
		renderForTask(null, getCurrentTask(), true, graphics);

		AITask previousTask = null;
		for (final AITask task : tasks) {
			renderForTask(previousTask, task, false, graphics);
			previousTask = task;
		}
	}


	@Override
	public void setCurrentTask(final AITask currentTask) {
		getHost().setTravelIconTimer(0f);
		super.setCurrentTask(currentTask);
	}


	private void renderForTask(final AITask previousTask, final AITask task, final boolean isCurrentTask, final Graphics graphics) {
		if (task instanceof JitGoToLocation) {

			float offset = 0f;
			if (isCurrentTask) {
				offset = (float) Math.cos(getHost().getTravelIconTimer() + Math.PI) + 1f;
			}

			if (userInterface.DEBUG) {
				final GoToLocation goToLocation = (GoToLocation)((JitGoToLocation)task).getTask();
				if (goToLocation != null) {
					goToLocation.renderPath();
				}
			}

			final Vector2 waypoint = ((JitGoToLocation) task).getDestination().waypoint.cpy();
			graphics.getSpriteBatch().setShader(Shaders.pass);
			Shaders.pass.setUniformMatrix("u_projTrans", userInterface.getUITrackingCamera().combined);
			graphics.getSpriteBatch().draw(UserInterface.finalWaypointTexture, waypoint.x - UserInterface.finalWaypointTexture.getRegionWidth()/2, waypoint.y + offset * 10f);
		} else if (task instanceof Jump) {
			Vector2 start = null;

			if (previousTask instanceof JitGoToLocation) {
				start = ((JitGoToLocation) previousTask).getDestination().waypoint.cpy();
			} else {
				final AITask currentTask = getCurrentTask();
				if (currentTask instanceof JitGoToLocation) {
					start = ((JitGoToLocation) currentTask).getDestination().waypoint;
				}
			}

			if (start != null) {
				final Vector2 waypoint = ((Jump) task).getDestination();
				userInterface.renderJumpArrow(
					start,
					waypoint
				);
			}
		}
	}


	@Override
	public WayPoint provideNextWaypoint() {
		final AITask current = getCurrentTask();
		if (current instanceof NextWaypointProvider) {
			return ((NextWaypointProvider) current).provideNextWaypoint();
		}
		return null;
	}
}