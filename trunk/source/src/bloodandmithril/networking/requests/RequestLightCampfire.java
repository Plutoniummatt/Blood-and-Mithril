package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.LightLightable;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A {@link Request} that instructs an individual to {@link LightLightable}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class RequestLightCampfire implements Request {

	private final int individualId, lightable;

	/**
	 * Constructor
	 */
	public RequestLightCampfire(Individual individual, Lightable lightable) {
		this.individualId = individual.getId().getId();
		this.lightable = ((Prop) lightable).id;
	}


	@Override
	public Responses respond() {
		Individual individual = Domain.getIndividual(individualId);
		try {
			individual.getAI().setCurrentTask(new LightLightable(individual, (Lightable) Domain.getWorld(Domain.getIndividual(individualId).getWorldId()).props().getProp(lightable), false));
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