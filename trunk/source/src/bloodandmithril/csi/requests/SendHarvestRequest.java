package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.Harvest;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.GameWorld;

/**
 * A {@link Request} for an {@link Individual} to {@link Harvest} a {@link Harvestable}
 *
 * @author Copyright (c) CHP Consulting Ltd. 2014
 */
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
		Individual individual = GameWorld.individuals.get(individualId);
		Prop prop = GameWorld.props.get(propId);
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