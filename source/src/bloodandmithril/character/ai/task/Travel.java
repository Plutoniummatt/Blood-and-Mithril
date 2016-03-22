package bloodandmithril.character.ai.task;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.Speech;
import bloodandmithril.character.ai.AIProcessor.JitGoToLocation;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.NextWaypointProvider;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
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

	/**
	 * Constructor
	 */
	public Travel(IndividualIdentifier hostId) {
		super(hostId, "Travelling");
	}


	/**
	 * Adds a {@link GoToLocation} task
	 */
	public void addGotoLocation(JitGoToLocation goToLocation) {
		appendTask(goToLocation);
		if (getHost().isSelected() && Util.roll(0.3f)) {
			getHost().speak(Speech.getRandomAffirmativeSpeech(), 1000);
			getHost().playAffirmativeSound();
		}
	}


	/**
	 * Adds a {@link GoToLocation} task
	 */
	public void addJump(Jump jump) {
		if (getCurrentTask() instanceof Jump || tasks.peekLast() instanceof Jump) {
			return;
		}
		appendTask(jump);
	}


	public Vector2 getDestination() {
		AITask task = getCurrentTask();
		if (task instanceof GoToLocation) {
			return ((GoToLocation) task).getPath().getDestinationWayPoint().waypoint.cpy();
		} else {
			return ((Jump) task).getDestination();
		}
	}


	public Vector2 getFinalGoToLocationWaypoint() {
		AITask peekLast = tasks.peekLast();
		if (peekLast == null) {
			AITask currentTask = getCurrentTask();
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
	public void renderWaypoints(Graphics graphics) {
		renderForTask(null, getCurrentTask(), true, graphics);

		AITask previousTask = null;
		for (AITask task : tasks) {
			renderForTask(previousTask, task, false, graphics);
			previousTask = task;
		}
	}


	@Override
	public void setCurrentTask(AITask currentTask) {
		getHost().setTravelIconTimer(0f);
		super.setCurrentTask(currentTask);
	}


	private void renderForTask(AITask previousTask, AITask task, boolean isCurrentTask, Graphics graphics) {
		if (task instanceof JitGoToLocation) {

			float offset = 0f;
			if (isCurrentTask) {
				offset = (float) Math.cos(getHost().getTravelIconTimer() + Math.PI) + 1f;
			}

			if (UserInterface.DEBUG) {
				GoToLocation goToLocation = (GoToLocation)((JitGoToLocation)task).getTask();
				if (goToLocation != null) {
					goToLocation.renderPath();
				}
			}

			Vector2 waypoint = ((JitGoToLocation) task).getDestination().waypoint.cpy();
			graphics.getSpriteBatch().setShader(Shaders.pass);
			Shaders.pass.setUniformMatrix("u_projTrans", UserInterface.UICameraTrackingCam.combined);
			graphics.getSpriteBatch().draw(UserInterface.finalWaypointTexture, waypoint.x - UserInterface.finalWaypointTexture.getRegionWidth()/2, waypoint.y + offset * 10f);
		} else if (task instanceof Jump) {
			Vector2 start = null;

			if (previousTask instanceof JitGoToLocation) {
				start = ((JitGoToLocation) previousTask).getDestination().waypoint.cpy();
			} else {
				AITask currentTask = getCurrentTask();
				if (currentTask instanceof JitGoToLocation) {
					start = ((JitGoToLocation) currentTask).getDestination().waypoint;
				}
			}

			if (start != null) {
				Vector2 waypoint = ((Jump) task).getDestination();
				UserInterface.renderJumpArrow(
					start,
					waypoint
				);
			}
		}
	}


	@Override
	public WayPoint provideNextWaypoint() {
		AITask current = getCurrentTask();
		if (current instanceof NextWaypointProvider) {
			return ((NextWaypointProvider) current).provideNextWaypoint();
		}
		return null;
	}
}