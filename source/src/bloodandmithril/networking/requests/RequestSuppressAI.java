package bloodandmithril.networking.requests;

import com.google.inject.Inject;

import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.playerinteraction.individual.api.IndividualAISupressionService;
import bloodandmithril.world.Domain;

/**
 * {@link Request} for {@link ArtificialIntelligence} suppression
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class RequestSuppressAI implements Request {

	@Inject
	private transient IndividualAISupressionService individualAISupressionService;


	private int individualId;
	private boolean suppressAI;

	/**
	 * Constructor
	 */
	public RequestSuppressAI(final Individual individual, final boolean suppressAI) {
		this.individualId = individual.getId().getId();
		this.suppressAI = suppressAI;
	}


	@Override
	public Responses respond() {
		individualAISupressionService.setAIsupression(Domain.getIndividual(individualId), suppressAI);;
		return new Responses(false);
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}
}