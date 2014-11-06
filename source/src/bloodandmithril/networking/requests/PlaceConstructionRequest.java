package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to place a construction
 *
 * @author Matt
 */
@Copyright("Matthew Peck")
public class PlaceConstructionRequest implements Request {

	private final Construction construction;
	private final int worldId;
	private final float x;
	private final float y;

	/**
	 * Constructor
	 */
	public PlaceConstructionRequest(Construction construction, float x, float y, int worldId) {
		this.construction = construction;
		this.x = x;
		this.y = y;
		this.worldId = worldId;
	}


	@Override
	public Responses respond() {
		if (construction.canBuildAt(x, y)) {
			Domain.addProp(construction, worldId);
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