package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.OpenCraftingStation;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.world.Domain;

/**
 * Request to tell the server that an {@link Individual} would like to open a {@link CraftingStation}.
 */
public class CSIOpenCraftingStation implements Request {

	private final int craftingStationId;
	private final int individualId;
	private final int connectionId;

	/**
	 * Constructor
	 */
	public CSIOpenCraftingStation(int individualId, int craftingStationId, int connectionId) {
		this.individualId = individualId;
		this.craftingStationId = craftingStationId;
		this.connectionId = connectionId;
	}


	@Override
	public Responses respond() {
		Responses response = new Response.Responses(false);

		Individual individual = Domain.getIndividuals().get(individualId);
		Prop prop = Domain.getProp(craftingStationId);
		if (prop instanceof CraftingStation) {
			individual.getAI().setCurrentTask(
				new OpenCraftingStation(individual, (CraftingStation) prop, connectionId)
			);
		}

		return response;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}


	public static class NotifyOpenCraftingStationWindow implements Response {

		private final int individualId;
		private final int craftingStationId;

		public NotifyOpenCraftingStationWindow(int individualId, int craftingStationId) {
			this.individualId = individualId;
			this.craftingStationId = craftingStationId;
		}

		@Override
		public void acknowledge() {
			Prop prop = Domain.getProp(craftingStationId);
			if (prop instanceof CraftingStation) {
				OpenCraftingStation.openCraftingStationWindow(Domain.getIndividuals().get(individualId), (CraftingStation)prop);
			}
		}

		@Override
		public int forClient() {
			return -1;
		}

		@Override
		public void prepare() {
		}
	}
}