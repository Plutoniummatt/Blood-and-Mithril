package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to consume a {@link Consumable}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ConsumeItem implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1880847101299030896L;
	private final int individualId;
	private final Consumable consumable;

	/**
	 * Constructor
	 */
	public ConsumeItem(final Consumable consumable, final int individualId) {
		this.consumable = consumable;
		this.individualId = individualId;
	}


	@Override
	public Responses respond() {
		final Responses responses = new Responses(true);

		final Individual individual = Domain.getIndividual(individualId);

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