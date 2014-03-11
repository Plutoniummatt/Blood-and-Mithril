package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to select/deselect an {@link Individual}
 */
public class IndividualSelection implements Request {

	/** id of the {@link Individual} to be selected or deselected */
	public final int individualId;

	/** true if selecting, otherwise false */
	public final boolean select;

	private int clientId;

	/**
	 * Constructor
	 */
	public IndividualSelection(int individualId, boolean select, int clientId) {
		this.individualId = individualId;
		this.select = select;
		this.clientId = clientId;
	}


	@Override
	public Responses respond() {
		Individual individual = Domain.getIndividuals().get(individualId);
		if (select) {
			individual.select(clientId);
			Domain.getSelectedIndividuals().remove(individual);
			Domain.getSelectedIndividuals().add(individual);
		} else {
			individual.deselect(false, clientId);
			Domain.getSelectedIndividuals().remove(individual);
		}
		Response response = new SelectIndividualResponse(individualId, select);
		Responses responses = new Response.Responses(false);
		responses.add(response);
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


	public static class SelectIndividualResponse implements Response {

		/** id of the {@link Individual} to be selected */
		public final int individualId;

		/** true if selecting, otherwise false */
		public final boolean select;

		/**
		 * Constructor
		 */
		public SelectIndividualResponse(int individualId, boolean select) {
			this.individualId = individualId;
			this.select = select;
		}

		@Override
		public void acknowledge() {
			Individual individual = Domain.getIndividuals().get(individualId);
			if (select) {
				individual.select(0);
				Domain.getSelectedIndividuals().remove(individual);
				Domain.getSelectedIndividuals().add(individual);
			} else {
				individual.deselect(false, 0);
				Domain.getSelectedIndividuals().remove(individual);
			}
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