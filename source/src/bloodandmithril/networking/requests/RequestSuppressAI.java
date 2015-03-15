package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
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
		Domain.getIndividual(individualId).setAISuppression(suppressAI);
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