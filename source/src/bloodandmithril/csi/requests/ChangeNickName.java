package bloodandmithril.csi.requests;

import java.util.LinkedList;

import bloodandmithril.character.Individual;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.world.GameWorld;

/**
 * {@link Request} to change an {@link Individual}'s nickname
 * 
 * @author Matt
 */
public class ChangeNickName implements Request {
	
	/** ID of the {@link Individual} to change name nick for */
	private final int individualId;
	private String toChangeTo;
	
	/**
	 * Constructor
	 */
	public ChangeNickName(int individualId, String toChangeTo) {
		this.individualId = individualId;
		this.toChangeTo = toChangeTo;
	}
	

	@Override
	public Responses respond() {
		Individual individual = GameWorld.individuals.get(individualId);
		if (individual != null) {
			individual.id.nickName = toChangeTo;
		}
		
		Responses responses = new Responses(false, new LinkedList<Response>());
		responses.responses.add(new ChangeNickNameResponse());
		return responses;
	}

	
	@Override
	public boolean tcp() {
		return false;
	}

	
	@Override
	public boolean notifyOthers() {
		return false;
	}
	
	
	public static class ChangeNickNameResponse implements Response {
		@Override
		public void acknowledge() {
			// Do nothing
		}

		@Override
		public int forClient() {
			return -1;
		}
	}
}