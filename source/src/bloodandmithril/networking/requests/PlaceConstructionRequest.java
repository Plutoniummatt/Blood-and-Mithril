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
	private final float x;
	private final float y;

	/**
	 * Constructor
	 */
	public PlaceConstructionRequest(Construction construction, float x, float y) {
		this.construction = construction;
		this.x = x;
		this.y = y;
	}


	@Override
	public Responses respond() {
		if (construction.canBuildAt(x, y)) {
			Domain.getProps().put(construction.id, construction);
			Domain.getActiveWorld().getProps().add(construction.id);
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