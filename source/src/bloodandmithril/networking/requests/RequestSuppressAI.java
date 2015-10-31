package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
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
	private int individualId;
	private boolean suppressAI;
	
	/**
	 * Constructor
	 */
	public RequestSuppressAI(Individual individual, boolean suppressAI) {
		this.individualId = individual.getId().getId();
		this.suppressAI = suppressAI;
	}
	

	@Override
	public Responses respond() {
		Wiring.injector().getInstance(IndividualAISupressionService.class).setAIsupression(Domain.getIndividual(individualId), suppressAI);;
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