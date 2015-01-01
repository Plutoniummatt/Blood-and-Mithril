package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AIProcessor.JitGoToLocation;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.Shaders;

import com.badlogic.gdx.math.Vector2;

/**
 * A {@link Travel} task is an ordered series of {@link GoToLocation}s and {@link Jump}s that get executed sequentially
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Travel extends CompositeAITask {
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
	public void renderWaypoints() {
		renderForTask(null, getCurrentTask());
		
		AITask previousTask = null;
		for (AITask task : tasks) {
			renderForTask(previousTask, task);
			previousTask = task;
		}
	}


	private void renderForTask(AITask previousTask, AITask task) {
		if (task instanceof JitGoToLocation) {
			Vector2 waypoint = ((JitGoToLocation) task).getDestination().waypoint.cpy();
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
			Shaders.pass.setUniformMatrix("u_projTrans", UserInterface.UICameraTrackingCam.combined);
			BloodAndMithrilClient.spriteBatch.draw(UserInterface.finalWaypointTexture, waypoint.x - UserInterface.finalWaypointTexture.getRegionWidth()/2, waypoint.y);
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
}