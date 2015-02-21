package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.PlaceProp;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.PropItem;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to place a prop
 *
 * @author Matt
 */
@Copyright("Matthew Peck")
public class PlacePropRequest implements Request {

	private final Prop prop;
	private final int worldId;
	private final float x;
	private final float y;
	private final Integer individualId;
	private final PropItem propItem;

	/**
	 * Constructor
	 * @param i 
	 */
	public PlacePropRequest(PropItem propItem, Integer individualId, Prop prop, float x, float y, int worldId) {
		this.propItem = propItem;
		this.individualId = individualId;
		this.prop = prop;
		this.x = x;
		this.y = y;
		this.worldId = worldId;
	}


	@Override
	public Responses respond() {
		if (prop.canPlaceAt(prop.position)) {
			if (individualId == null) {
				Domain.getWorld(worldId).props().addProp(prop);
			} else {
				Individual individual = Domain.getIndividual(individualId);
				individual.getAI().setCurrentTask(new PlaceProp(individual, prop.position, propItem));
			}
		}

		return new Responses(false);
	}


	@Override
	public boolean tcp() {
		return false;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}
}