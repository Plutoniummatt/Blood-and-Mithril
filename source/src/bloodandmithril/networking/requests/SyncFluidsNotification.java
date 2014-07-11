package bloodandmithril.networking.requests;

import bloodandmithril.networking.Response;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.fluid.FluidMap;

/**
 * A {@link Response} that synchronizes the {@link FluidMap} on the active world.
 *
 * @author Matt
 */
public class SyncFluidsNotification implements Response {
	
	private FluidMap toSend;

	/**
	 * Constructor
	 */
	public SyncFluidsNotification(FluidMap toSend) {
		synchronized (toSend) {
			this.toSend = toSend.deepCopy();
		}
	}
	

	@Override
	public void acknowledge() {
		Domain.getActiveWorld().getTopography().setFluids(toSend);
	}
	

	@Override
	public int forClient() {
		return -1;
	}
	

	@Override
	public void prepare() {
	}
}