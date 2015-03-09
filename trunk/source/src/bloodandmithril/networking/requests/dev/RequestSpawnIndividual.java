package bloodandmithril.networking.requests.dev;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to spawn an {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class RequestSpawnIndividual implements Request {

	private Individual individual;

	/**
	 * Constructor
	 */
	public RequestSpawnIndividual(Individual individual) {
		this.individual = individual;
	}


	@Override
	public Responses respond() {
		Domain.addIndividual(individual, individual.getWorldId());
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