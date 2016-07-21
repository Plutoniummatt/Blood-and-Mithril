package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to toggle walk/run
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ToggleWalkRun implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3361191917549334543L;
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
		Domain.getIndividual(individualId).setWalking(walk);
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