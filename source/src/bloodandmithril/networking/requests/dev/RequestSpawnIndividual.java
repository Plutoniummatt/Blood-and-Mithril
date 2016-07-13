package bloodandmithril.networking.requests.dev;

import com.google.inject.Inject;

import bloodandmithril.character.AddIndividualService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;

/**
 * {@link Request} to spawn an {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class RequestSpawnIndividual implements Request {

	private Individual individual;

	@Inject
	private transient AddIndividualService addIndividualService;

	/**
	 * Constructor
	 */
	public RequestSpawnIndividual(final Individual individual) {
		this.individual = individual;
	}


	@Override
	public Responses respond() {
		addIndividualService.addIndividual(individual, individual.getWorldId());
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