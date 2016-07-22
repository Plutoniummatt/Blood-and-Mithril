package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.construct.Construct;
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
	private static final long serialVersionUID = 7191403413062601744L;

	private int individualId, constructionId;
	private final boolean deconstruct;

	/**
	 * Constructor
	 */
	public ConstructionRequest(final int individualId, final int constructionId, final boolean deconstruct) {
		this.individualId = individualId;
		this.constructionId = constructionId;
		this.deconstruct = deconstruct;
	}


	@Override
	public Responses respond() {
		final Individual individual = Domain.getIndividual(individualId);
		individual.getAI().setCurrentTask(new Construct(individual, (Construction)Domain.getWorld(individual.getWorldId()).props().getProp(constructionId), deconstruct));
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