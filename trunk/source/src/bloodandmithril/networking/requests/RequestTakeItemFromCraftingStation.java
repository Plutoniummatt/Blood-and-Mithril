package bloodandmithril.networking.requests;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.requests.RefreshWindows.RefreshWindowsResponse;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} to take a finished item from a {@link CraftingStation}
 *
 * @author Matt
 */
public class RequestTakeItemFromCraftingStation implements Request {

	private final int individualId;
	private final int craftingStationId;

	/**
	 * Constructor
	 */
	public RequestTakeItemFromCraftingStation(Individual individual, CraftingStation craftingStation) {
		this.individualId = individual.getId().getId();
		this.craftingStationId = craftingStation.id;
	}


	@Override
	public Responses respond() {
		((CraftingStation)Domain.getProps().get(craftingStationId)).takeItem(Domain.getIndividuals().get(individualId));

		Responses responses = new Responses(true);
		responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(individualId, System.currentTimeMillis()));
		responses.add(new SynchronizePropRequest.SynchronizePropResponse(Domain.getProps().get(craftingStationId)));
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