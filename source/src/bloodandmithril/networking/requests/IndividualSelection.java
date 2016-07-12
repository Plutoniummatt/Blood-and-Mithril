package bloodandmithril.networking.requests;

import com.google.inject.Inject;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.playerinteraction.individual.api.IndividualSelectionService;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to select/deselect an {@link Individual}
 */
@Copyright("Matthew Peck 2014")
public class IndividualSelection implements Request {

	/** id of the {@link Individual} to be selected or deselected */
	public final int individualId;

	/** true if selecting, otherwise false */
	public final boolean select;

	private int clientId;

	@Inject
	private transient GameClientStateTracker gameClientStateTracker;

	@Inject
	private transient IndividualSelectionService individualSelectionService;

	/**
	 * Constructor
	 */
	public IndividualSelection(final int individualId, final boolean select, final int clientId) {
		this.individualId = individualId;
		this.select = select;
		this.clientId = clientId;
	}


	@Override
	public Responses respond() {
		final Individual individual = Domain.getIndividual(individualId);
		if (select) {
			individualSelectionService.select(individual, clientId);
			gameClientStateTracker.removeSelectedIndividual(individual);
			gameClientStateTracker.addSelectedIndividual(individual);
		} else {
			individual.deselect(false, clientId);
			gameClientStateTracker.removeSelectedIndividual(individual);
		}
		final Response response = new SelectIndividualResponse(individualId, select);
		final Responses responses = new Response.Responses(false);
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

		@Inject
		private transient GameClientStateTracker gameClientStateTracker;

		@Inject
		private transient IndividualSelectionService individualSelectionService;

		/** id of the {@link Individual} to be selected */
		public final int individualId;

		/** true if selecting, otherwise false */
		public final boolean select;

		/**
		 * Constructor
		 */
		public SelectIndividualResponse(final int individualId, final boolean select) {
			this.individualId = individualId;
			this.select = select;
		}

		@Override
		public void acknowledge() {
			final Individual individual = Domain.getIndividual(individualId);
			if (select) {
				individualSelectionService.select(individual, 0);
				gameClientStateTracker.removeSelectedIndividual(individual);
				gameClientStateTracker.addSelectedIndividual(individual);
			} else {
				individual.deselect(false, 0);
				gameClientStateTracker.removeSelectedIndividual(individual);
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