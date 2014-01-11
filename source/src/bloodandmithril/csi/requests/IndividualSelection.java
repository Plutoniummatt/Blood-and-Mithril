package bloodandmithril.csi.requests;

import java.util.LinkedList;

import bloodandmithril.character.Individual;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.world.GameWorld;

/**
 * {@link Request} to select/deselect an {@link Individual}
 */
public class IndividualSelection implements Request {

	/** id of the {@link Individual} to be selected or deselected */
	public final int individualId;

	/** true if selecting, otherwise false */
	public final boolean select;

	/**
	 * Constructor
	 */
	public IndividualSelection(int individualId, boolean select) {
		this.individualId = individualId;
		this.select = select;
	}


	@Override
	public Responses respond() {
		Individual individual = GameWorld.individuals.get(individualId);
		if (select) {
			individual.select();
			if (!GameWorld.selectedIndividuals.remove(individual)) {
				GameWorld.selectedIndividuals.add(individual);
			}
		} else {
			individual.deselect(false);
			GameWorld.selectedIndividuals.remove(individual);
		}
		Response response = new SelectIndividualResponse(individualId, select);
		Responses responses = new Response.Responses(false, new LinkedList<Response>());
		responses.responses.add(response);
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
			Individual individual = GameWorld.individuals.get(individualId);
			if (select) {
				individual.select();
				if (!GameWorld.selectedIndividuals.remove(individual)) {
					GameWorld.selectedIndividuals.add(individual);
				}
			} else {
				individual.deselect(false);
				GameWorld.selectedIndividuals.remove(individual);
			}
		}
		
		@Override
		public int forClient() {
			return -1;
		}
	}
}