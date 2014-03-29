package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.Construct;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.prop.building.Construction;
import bloodandmithril.world.Domain;

/**
 * {@link Request} the construction of a {@link Construction}
 *
 * @author Matt
 */
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
		Individual individual = Domain.getIndividuals().get(individualId);
		individual.getAI().setCurrentTask(new Construct(individual, (Construction)Domain.getProps().get(constructionId)));
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