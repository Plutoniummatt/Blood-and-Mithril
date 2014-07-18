package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} to discard an item from the inventory of an {@link Individual}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class RequestDiscardItem implements Request {

	private final Item item;
	private final int quantity;
	private final int hostId;

	public RequestDiscardItem(Individual host, Item item, int quantity) {
		this.item = item;
		this.quantity = quantity;
		this.hostId = host.getId().getId();
	}


	@Override
	public Responses respond() {
		ContainerImpl.discard(
			Domain.getIndividuals().get(hostId),
			item,
			quantity
		);

		Responses responses = new Responses(true);
		responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(hostId, System.currentTimeMillis()));
		responses.add(new SynchronizeItems());
		responses.add(new RefreshWindows.RefreshWindowsResponse());
		return responses;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return true;
	}
}