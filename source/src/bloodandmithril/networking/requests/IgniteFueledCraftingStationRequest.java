package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.FueledCraftingStation;
import bloodandmithril.world.Domain;

@Copyright("Matthew Peck 2014")
public class IgniteFueledCraftingStationRequest implements Request {

	private int furnaceId;
	private int worldId;

	/**
	 * Constructor
	 */
	public IgniteFueledCraftingStationRequest(int furnaceId, int worldId) {
		this.furnaceId = furnaceId;
		this.worldId = worldId;
	}


	@Override
	public Responses respond() {
		Prop prop = Domain.getWorld(worldId).props().getProp(furnaceId);
		if (prop instanceof FueledCraftingStation) {
			if (!((FueledCraftingStation) prop).isBurning()) {
				((FueledCraftingStation) prop).ignite();
			}
		} else {
			throw new RuntimeException("Expected a furnace, but got a " + prop.getClass().getSimpleName());
		}

		return new Responses(false);
	}


	@Override
	public boolean tcp() {
		return false;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}
}