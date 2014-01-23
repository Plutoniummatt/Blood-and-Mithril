package bloodandmithril.csi.requests;

import java.util.LinkedList;

import bloodandmithril.character.Individual;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.item.material.container.Bottle;
import bloodandmithril.item.material.liquid.Liquid;
import bloodandmithril.world.GameWorld;

/**
 * A {@link Request} to drink {@link Liquid}.
 *
 * @author Matt
 */
public class DrinkLiquid implements Request {

	private final int individualId;
	private final Bottle bottleToDrinkFrom;
	private final float amount;

	/**
	 * Constructor
	 */
	public DrinkLiquid(int individualId, Bottle bottleToDrinkFrom, float amount) {
		this.individualId = individualId;
		this.bottleToDrinkFrom = bottleToDrinkFrom;
		this.amount = amount;
	}


	@Override
	public Responses respond() {

		Individual individual = GameWorld.individuals.get(individualId);
		if (individual.takeItem(bottleToDrinkFrom) == 1) {
			Bottle newBottle = bottleToDrinkFrom.clone();
			newBottle.drink(amount, individual);
			individual.giveItem(newBottle);
		}

		Responses responses = new Responses(true, new LinkedList<Response>());
		responses.responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(individual, System.currentTimeMillis()));
		responses.responses.add(new TransferItems.RefreshWindowsResponse());
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