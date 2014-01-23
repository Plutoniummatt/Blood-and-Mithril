package bloodandmithril.csi.requests;

import java.util.LinkedList;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.world.GameWorld;

/**
 * {@link Request} to toggle walk/run
 *
 * @author Matt
 */
public class ToggleWalkRun implements Request {

	private final boolean walk;
	private final int individualId;

	/**
	 * Constructor
	 */
	public ToggleWalkRun(int individualId, boolean walk) {
		this.individualId = individualId;
		this.walk = walk;
	}


	@Override
	public Responses respond() {
		GameWorld.individuals.get(individualId).setWalking(walk);
		return new Responses(false, new LinkedList<Response>());
	}


	@Override
	public boolean tcp() {
		return false;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}
}