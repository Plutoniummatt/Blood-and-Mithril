package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Response;
import bloodandmithril.world.Domain;

/**
 * A {@link Response} to notifiy clients that a prop has been removed
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class DestroyPropNotification implements Response {

	private final int propId;

	/** Constructor */
	public DestroyPropNotification(int propId) {
		this.propId = propId;
	}


	@Override
	public void acknowledge() {
		Domain.removeProp(propId);
	}


	@Override
	public int forClient() {
		return -1;
	}


	@Override
	public void prepare() {
	}
}