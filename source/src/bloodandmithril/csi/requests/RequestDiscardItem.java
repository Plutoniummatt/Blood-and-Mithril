package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} to discard an item from the inventory of an {@link Individual}
 *
 * @author Matt
 */
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