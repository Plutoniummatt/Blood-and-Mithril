package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.ConstructDeconstruct;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.world.Domain;

@Copyright("Matthew Peck 2016")
public class RequestConstructDeconstruct implements Request {

	/**
	 *
	 */
	private static final long serialVersionUID = -299313925434137465L;
	private final int individualId;
	private final int propId;
	private final int connectionId;

	/**
	 * Constructor
	 */
	public RequestConstructDeconstruct(final int individualId, final int propId, final int connectionId) {
		this.individualId = individualId;
		this.propId = propId;
		this.connectionId = connectionId;
	}


	@Override
	public Responses respond() {
		final Individual constructor = Domain.getIndividual(individualId);
		final Construction construction = (Construction) Domain.getWorld(constructor.getWorldId()).props().getProp(propId);
		constructor.getAI().setCurrentTask(new ConstructDeconstruct(constructor, construction, connectionId));

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