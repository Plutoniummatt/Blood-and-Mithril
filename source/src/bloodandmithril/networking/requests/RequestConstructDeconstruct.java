package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.ConstructDeconstruct;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.world.Domain;

public class RequestConstructDeconstruct implements Request {
	
	private final int individualId;
	private final int propId;
	private final int connectionId;

	/**
	 * Constructor
	 */
	public RequestConstructDeconstruct(int individualId, int propId, int connectionId) {
		this.individualId = individualId;
		this.propId = propId;
		this.connectionId = connectionId;
	}

	
	@Override
	public Responses respond() {
		Individual constructor = Domain.getIndividual(individualId);
		Construction construction = (Construction) Domain.getWorld(constructor.getWorldId()).props().getProp(propId);
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