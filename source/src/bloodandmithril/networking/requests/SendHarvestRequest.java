package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.Harvest;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} for an {@link Individual} to {@link Harvest} a {@link Harvestable}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class SendHarvestRequest implements Request {

	private final int individualId;
	private final int propId;

	/**
	 * Constructor
	 */
	public SendHarvestRequest(int individualId, int propId) {
		this.individualId = individualId;
		this.propId = propId;
	}


	@Override
	public Responses respond() {
		Individual individual = Domain.getIndividual(individualId);
		Prop prop = Domain.getWorld(individual.getWorldId()).props().getProp(propId);
		Harvestable harvestable = null;

		if (!(prop instanceof Harvestable)) {
			throw new RuntimeException("Can not harvest " + prop.getClass().getSimpleName());
		} else {
			harvestable = (Harvestable) prop;
		}

		individual.getAI().setCurrentTask(
			new Harvest(individual, harvestable)
		);

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