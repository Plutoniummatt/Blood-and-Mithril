package bloodandmithril.csi.requests;

import bloodandmithril.csi.Response;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.fluid.FluidMap;

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