package bloodandmithril.networking.requests;

import java.util.Collection;

import bloodandmithril.character.ai.task.TakeItem;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

import com.google.common.collect.Lists;

/**
 * A {@link Request} for an {@link Individual} to {@link TakeItem}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class RequestTakeItem implements Request {

	private final int individualId;
	private Collection<Integer> items = Lists.newArrayList();

	/**
	 * Constructor
	 */
	public RequestTakeItem(Individual individual, Collection<Item> items) {
		this.individualId = individual.getId().getId();

		for (Item item : items) {
			this.items.add(item.getId());
		}
	}


	@Override
	public Responses respond() {
		Individual individual = Domain.getIndividual(individualId);
		Collection<Item> serverItems = Lists.newArrayList();
		for (Integer id : items) {
			Item item = Domain.getItems().get(id);
			if (item != null) {
				serverItems.add(item);
			}
		}

		individual.getAI().setCurrentTask(new TakeItem(individual, serverItems));
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