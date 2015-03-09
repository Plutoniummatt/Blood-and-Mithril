package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to instruct an {@link Individual} to speak/not speak
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualSpeakRequest implements Request {
	private boolean speak;
	private int individualId;

	/**
	 * Constructor
	 */
	public IndividualSpeakRequest(Individual individual, boolean speak) {
		this.speak = speak;
		this.individualId = individual.getId().getId();
	}


	@Override
	public Responses respond() {
		Domain.getIndividual(individualId).setShutUp(!speak);
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