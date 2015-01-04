package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.LightCampfire;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.construction.craftingstation.Campfire;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A {@link Request} that instructs an individual to {@link LightCampfire}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class RequestLightCampfire implements Request {

	private final int individualId, campfireId;

	/**
	 * Constructor
	 */
	public RequestLightCampfire(Individual individual, Campfire campfire) {
		this.individualId = individual.getId().getId();
		this.campfireId = campfire.id;
	}


	@Override
	public Responses respond() {
		Individual individual = Domain.getIndividual(individualId);
		try {
			individual.getAI().setCurrentTask(new LightCampfire(individual, (Campfire) Domain.getWorld(Domain.getIndividual(individualId).getWorldId()).props().getProp(campfireId)));
		} catch (NoTileFoundException e) {}
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