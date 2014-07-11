package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * Changes the biography of an individual
 *
 * @author Matt
 */
public class ChangeIndividualBiography implements Request {

	private final int individualId;
	private final String description;

	/**
	 * Construction
	 */
	public ChangeIndividualBiography(Individual individual, String description) {
		this.individualId = individual.getId().getId();
		this.description = description;
	}


	@Override
	public Responses respond() {
		Domain.getIndividuals().get(individualId).updateDescription(description);
		Responses responses = new Responses(false);
		responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(individualId, System.currentTimeMillis()));
		return responses;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return true;
	}
}