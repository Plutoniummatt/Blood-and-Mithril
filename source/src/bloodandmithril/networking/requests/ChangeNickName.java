package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to change an {@link Individual}'s nickname
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ChangeNickName implements Request {

	/** ID of the {@link Individual} to change name nick for */
	private final int individualId;
	private final String toChangeTo;

	/**
	 * Constructor
	 */
	public ChangeNickName(int individualId, String toChangeTo) {
		this.individualId = individualId;
		this.toChangeTo = toChangeTo;
	}


	@Override
	public Responses respond() {
		Individual individual = Domain.getIndividuals().get(individualId);
		if (individual != null) {
			individual.getId().setNickName(toChangeTo);
		}

		Responses responses = new Responses(false);
		responses.add(new ChangeNickNameResponse());
		return responses;
	}


	@Override
	public boolean tcp() {
		return true;
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

		@Override
		public void prepare() {
		}
	}
}