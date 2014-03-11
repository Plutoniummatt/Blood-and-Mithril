package bloodandmithril.csi.requests;

import bloodandmithril.csi.Response;
import bloodandmithril.world.Domain;

/**
 * A {@link Response} to notifiy clients that a prop has been removed
 *
 * @author Matt
 */
public class DestroyPropNotification implements Response {

	private final int propId;

	/** Constructor */
	public DestroyPropNotification(int propId) {
		this.propId = propId;
	}


	@Override
	public void acknowledge() {
		Domain.getProps().remove(propId);
	}


	@Override
	public int forClient() {
		return -1;
	}


	@Override
	public void prepare() {
	}
}