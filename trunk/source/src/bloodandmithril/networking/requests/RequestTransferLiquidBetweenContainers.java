package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.container.LiquidContainer;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * A request to transfer liquids between {@link LiquidContainer}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class RequestTransferLiquidBetweenContainers implements Request {

	private final int individualId;
	private final LiquidContainer from, to;
	private final float amount;

	/**
	 * Constructor
	 */
	public RequestTransferLiquidBetweenContainers(Individual individual, LiquidContainer from, LiquidContainer to, float amount) {
		this.individualId = individual.getId().getId();
		this.from = from;
		this.to = to;
		this.amount = amount;
	}


	@Override
	public Responses respond() {
		LiquidContainer.transfer(
			Domain.getIndividuals().get(individualId),
			from,
			to,
			amount
		);

		Responses responses = new Responses(true);
		responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(individualId, System.currentTimeMillis()));
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