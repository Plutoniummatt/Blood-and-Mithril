package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to place a prop
 *
 * @author Matt
 */
@Copyright("Matthew Peck")
public class PlacePropRequest implements Request {

	private final Prop prop;
	private final int worldId;
	private final float x;
	private final float y;

	/**
	 * Constructor
	 */
	public PlacePropRequest(Prop prop, float x, float y, int worldId) {
		this.prop = prop;
		this.x = x;
		this.y = y;
		this.worldId = worldId;
	}


	@Override
	public Responses respond() {
		if (prop.canPlaceAt(x, y)) {
			Domain.getWorld(worldId).props().addProp(prop);
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