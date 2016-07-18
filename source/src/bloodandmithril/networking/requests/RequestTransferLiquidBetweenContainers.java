package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.container.LiquidContainerItem;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * A request to transfer liquids between {@link LiquidContainerItem}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class RequestTransferLiquidBetweenContainers implements Request {

	private final int individualId;
	private final LiquidContainerItem from, to;
	private final float amount;

	/**
	 * Constructor
	 */
	public RequestTransferLiquidBetweenContainers(final Individual individual, final LiquidContainerItem from, final LiquidContainerItem to, final float amount) {
		this.individualId = individual.getId().getId();
		this.from = from;
		this.to = to;
		this.amount = amount;
	}


	@Override
	public Responses respond() {
		LiquidContainerItem.transfer(
			Domain.getIndividual(individualId),
			from,
			to,
			amount
		);

		final Responses responses = new Responses(true);
		responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(individualId, System.currentTimeMillis()));
		responses.add(new RefreshWindowsResponse());
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