package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.container.LiquidContainerItem;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} to drink {@link Liquid}.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class DrinkLiquid implements Request {

	private final int individualId;
	private final LiquidContainerItem bottleToDrinkFrom;
	private final float amount;

	/**
	 * Constructor
	 */
	public DrinkLiquid(final int individualId, final LiquidContainerItem bottleToDrinkFrom, final float amount) {
		this.individualId = individualId;
		this.bottleToDrinkFrom = bottleToDrinkFrom;
		this.amount = amount;
	}


	@Override
	public Responses respond() {

		final Individual individual = Domain.getIndividual(individualId);
		if (individual.takeItem(bottleToDrinkFrom) == 1) {
			final LiquidContainerItem newBottle = bottleToDrinkFrom.clone();
			newBottle.drinkFrom(amount, individual);
			individual.giveItem(newBottle);
		}

		final Responses responses = new Responses(true);
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