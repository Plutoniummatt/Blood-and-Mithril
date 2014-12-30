package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.Construct;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.world.Domain;

/**
 * {@link Request} the construction of a {@link Construction}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ConstructionRequest implements Request {

	private int individualId, constructionId;

	/**
	 * Constructor
	 */
	public ConstructionRequest(int individualId, int constructionId) {
		this.individualId = individualId;
		this.constructionId = constructionId;
	}


	@Override
	public Responses respond() {
		Individual individual = Domain.getIndividual(individualId);
		individual.getAI().setCurrentTask(new Construct(individual, (Construction)Domain.getWorld(individual.getWorldId()).props().getProp(constructionId)));
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