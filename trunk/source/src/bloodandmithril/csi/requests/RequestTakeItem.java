package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TakeItem;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.item.Item;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} for an {@link Individual} to {@link TakeItem}
 *
 * @author Matt
 */
public class RequestTakeItem implements Request {

	private final int individualId, itemId;

	/**
	 * Constructor
	 */
	public RequestTakeItem(Individual individual, Item item) {
		this.individualId = individual.getId().getId();
		this.itemId = item.getId();
	}


	@Override
	public Responses respond() {
		Individual individual = Domain.getIndividuals().get(individualId);
		individual.getAI().setCurrentTask(new TakeItem(individual, Domain.getItems().get(itemId)));
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