package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.items.container.LiquidContainer;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.requests.RefreshWindows.RefreshWindowsResponse;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} to drink {@link Liquid}.
 *
 * @author Matt
 */
public class DrinkLiquid implements Request {

	private final int individualId;
	private final LiquidContainer bottleToDrinkFrom;
	private final float amount;

	/**
	 * Constructor
	 */
	public DrinkLiquid(int individualId, LiquidContainer bottleToDrinkFrom, float amount) {
		this.individualId = individualId;
		this.bottleToDrinkFrom = bottleToDrinkFrom;
		this.amount = amount;
	}


	@Override
	public Responses respond() {

		Individual individual = Domain.getIndividuals().get(individualId);
		if (individual.takeItem(bottleToDrinkFrom) == 1) {
			LiquidContainer newBottle = bottleToDrinkFrom.clone();
			newBottle.drinkFrom(amount, individual);
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