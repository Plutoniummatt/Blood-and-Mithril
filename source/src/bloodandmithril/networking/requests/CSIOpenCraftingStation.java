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
	private static final long serialVersionUID = 4115875631842138064L;

	private final int craftingStationId;
	private final int individualId;
	private final int connectionId;
	private final int worldId;

	/**
	 * Constructor
	 */
	public CSIOpenCraftingStation(final int individualId, final int craftingStationId, final int connectionId, final int worldId) {
		this.individualId = individualId;
		this.craftingStationId = craftingStationId;
		this.connectionId = connectionId;
		this.worldId = worldId;
	}


	@Override
	public Responses respond() {
		final Responses response = new Response.Responses(false);

		final Individual individual = Domain.getIndividual(individualId);
		final Prop prop = Domain.getWorld(worldId).props().getProp(craftingStationId);
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

		/**
		 *
		 */
		private static final long serialVersionUID = 6302983218575053744L;
		private final int individualId;
		private final int craftingStationId;
		private final int worldId;

		public NotifyOpenCraftingStationWindow(final int individualId, final int craftingStationId, final int worldId) {
			this.individualId = individualId;
			this.craftingStationId = craftingStationId;
			this.worldId = worldId;
		}

		@Override
		public void acknowledge() {
			final Prop prop = Domain.getWorld(worldId).props().getProp(craftingStationId);
			if (prop instanceof CraftingStation) {
				OpenCraftingStation.openCraftingStationWindow(Domain.getIndividual(individualId), (CraftingStation)prop);
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