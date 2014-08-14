package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.container.LiquidContainer;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.requests.RefreshWindows.RefreshWindowsResponse;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to discard contents of a {@link LiquidContainer}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class DiscardLiquid implements Request {

	private final int individualId;
	private final LiquidContainer bottleToDiscardFrom;
	private final float amount;

	/**
	 * Constructor
	 */
	public DiscardLiquid(int individualId, LiquidContainer bottleToDiscardFrom, float amount) {
		this.individualId = individualId;
		this.bottleToDiscardFrom = bottleToDiscardFrom;
		this.amount = amount;
	}


	@Override
	public Responses respond() {

		Individual individual = Domain.getIndividuals().get(individualId);
		if (individual.takeItem(bottleToDiscardFrom) == 1) {
			LiquidContainer newBottle = bottleToDiscardFrom.clone();
			newBottle.subtract(amount);
			individual.giveItem(newBottle);
		}

		Responses responses = new Responses(true);
		responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(individual.getId().getId(), System.currentTimeMillis()));
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