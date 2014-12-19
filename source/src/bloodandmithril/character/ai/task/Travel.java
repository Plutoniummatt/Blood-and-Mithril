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
	
	
	/**
	 * Renders all waypoints in this {@link Travel} task
	 */
	public void renderWaypoints() {
		renderForTask(getCurrentTask());
		
		for (AITask task : tasks) {
			renderForTask(task);
		}
	}


	private void renderForTask(AITask task) {
		if (task instanceof JitGoToLocation) {
			Vector2 waypoint = ((JitGoToLocation) task).getDestination().waypoint.cpy();
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
			Shaders.pass.setUniformMatrix("u_projTrans", UserInterface.UICameraTrackingCam.combined);
			BloodAndMithrilClient.spriteBatch.draw(UserInterface.finalWaypointTexture, waypoint.x - UserInterface.finalWaypointTexture.getRegionWidth()/2, waypoint.y);
		} else if (task instanceof Jump) {
			Vector2 waypoint = ((Jump) task).getDestination();
			BloodAndMithrilClient.spriteBatch.setShader(Shaders.pass);
			Shaders.pass.setUniformMatrix("u_projTrans", UserInterface.UICameraTrackingCam.combined);
			BloodAndMithrilClient.spriteBatch.draw(UserInterface.finalWaypointTexture, waypoint.x - UserInterface.finalWaypointTexture.getRegionWidth()/2, waypoint.y);
		}
	}
}