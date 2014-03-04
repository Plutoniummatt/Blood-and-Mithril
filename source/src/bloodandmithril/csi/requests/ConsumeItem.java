package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Item;
import bloodandmithril.world.GameWorld;

/**
 * {@link Request} to consume a {@link Consumable}
 *
 * @author Matt
 */
public class ConsumeItem implements Request {

	private final int individualId;
	private final Consumable consumable;

	/**
	 * Constructor
	 */
	public ConsumeItem(Consumable consumable, int individualId) {
		this.consumable = consumable;
		this.individualId = individualId;
	}


	@Override
	public Responses respond() {
		Responses responses = new Responses(true);

		Individual individual = GameWorld.individuals.get(individualId);

		if (consumable.consume(individual)) {
			individual.takeItem((Item)consumable);
		}

		responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(individual.getId().getId(), System.currentTimeMillis()));
		responses.add(new TransferItems.RefreshWindowsResponse());

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