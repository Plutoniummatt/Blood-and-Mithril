package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} to take a finished item from a {@link CraftingStation}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class RequestTakeItemFromCraftingStation implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3172926961949806871L;
	private final int individualId;
	private final int craftingStationId;

	/**
	 * Constructor
	 */
	public RequestTakeItemFromCraftingStation(final Individual individual, final CraftingStation craftingStation) {
		this.individualId = individual.getId().getId();
		this.craftingStationId = craftingStation.id;
	}


	@Override
	public Responses respond() {
		((CraftingStation)Domain.getWorld(Domain.getIndividual(individualId).getWorldId()).props().getProp(craftingStationId)).takeItem(Domain.getIndividual(individualId));

		final Responses responses = new Responses(true);
		responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(individualId, System.currentTimeMillis()));
		responses.add(new SynchronizePropRequest.SynchronizePropResponse(Domain.getWorld(Domain.getIndividual(individualId).getWorldId()).props().getProp(craftingStationId)));
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