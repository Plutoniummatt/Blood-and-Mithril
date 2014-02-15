package bloodandmithril.csi.requests;

import java.util.LinkedList;
import java.util.Map.Entry;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.Fuel;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.world.GameWorld;

public class IgniteFurnaceRequest implements Request {

	private int furnaceId;

	/**
	 * Constructor
	 */
	public IgniteFurnaceRequest(int furnaceId) {
		this.furnaceId = furnaceId;
	}
	
	
	@Override
	public Responses respond() {
		Prop prop = GameWorld.props.get(furnaceId);
		if (prop instanceof Furnace) {
			float finalDuration = 0f;
			for (Entry<Item, Integer> entry : ((Furnace) prop).container.getInventory().entrySet()) {
				Item item = entry.getKey();
				if (item instanceof Fuel) {
					finalDuration = finalDuration + ((Fuel) item).getCombustionDuration() * entry.getValue() * (Furnace.minTemp / Furnace.minTemp);
				}
			}

			if (!((Furnace) prop).isBurning()) {
				((Furnace) prop).setCombustionDurationRemaining(finalDuration);
				((Furnace) prop).ignite();
			}
		} else {
			throw new RuntimeException("Expected a furnace, but got a " + prop.getClass().getSimpleName());
		}
		return new Responses(false, new LinkedList<Response>());
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