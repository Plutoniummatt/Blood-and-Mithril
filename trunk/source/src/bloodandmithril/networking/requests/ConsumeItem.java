package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.requests.RefreshWindows.RefreshWindowsResponse;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to consume a {@link Consumable}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
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

		Individual individual = Domain.getIndividuals().get(individualId);

		if (consumable.consume(individual)) {
			individual.takeItem((Item)consumable);
		}

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