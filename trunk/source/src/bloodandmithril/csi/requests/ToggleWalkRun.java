package bloodandmithril.csi.requests;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.world.Domain;

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
		Domain.individuals.get(individualId).setWalking(walk);
		return new Responses(false);
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