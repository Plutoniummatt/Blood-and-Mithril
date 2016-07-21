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

	/**
	 * 
	 */
	private static final long serialVersionUID = -5387337418293298104L;
	private final int propId;
	private final int worldId;

	/** Constructor */
	public DestroyPropNotification(int propId, int worldId) {
		this.propId = propId;
		this.worldId = worldId;
	}


	@Override
	public void acknowledge() {
		Domain.getWorld(worldId).props().removeProp(propId);
	}


	@Override
	public int forClient() {
		return -1;
	}


	@Override
	public void prepare() {
	}
}