package bloodandmithril.networking.requests;

import java.util.Map.Entry;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.Fuel;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.world.Domain;

@Copyright("Matthew Peck 2014")
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
		Prop prop = Domain.getProp(furnaceId);
		if (prop instanceof Furnace) {
			float finalDuration = 0f;
			for (Entry<Item, Integer> entry : ((Furnace) prop).getInventory().entrySet()) {
				Item item = entry.getKey();
				if (item instanceof Fuel) {
					finalDuration = finalDuration + ((Fuel) item).getCombustionDuration() * entry.getValue();
				}
			}

			if (!((Furnace) prop).isBurning()) {
				((Furnace) prop).setCombustionDurationRemaining(finalDuration);
				((Furnace) prop).ignite();
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