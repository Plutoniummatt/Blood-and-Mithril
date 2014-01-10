package bloodandmithril.csi.requests;

import java.util.List;

import bloodandmithril.character.Individual;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.world.GameWorld;

import com.google.common.collect.Lists;

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
	public List<Response> respond() {
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
		return Lists.newArrayList(response);
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
	}
}