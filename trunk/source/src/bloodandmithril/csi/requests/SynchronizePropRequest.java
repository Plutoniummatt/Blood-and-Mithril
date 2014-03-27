package bloodandmithril.csi.requests;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} to synchronize a {@link Prop}
 *
 * @author Matt
 */
public class SynchronizePropRequest implements Request {

	private final int propId;

	public SynchronizePropRequest(int propId) {
		this.propId = propId;
	}


	@Override
	public Responses respond() {
		Responses responses = new Responses(false);
		responses.add(new SynchronizePropResponse(Domain.getProps().get(propId)));
		responses.add(new TransferItems.RefreshWindowsResponse());
		return responses;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return true;
	}


	public static class SynchronizePropResponse implements Response {

		private final Prop prop;

		public SynchronizePropResponse(Prop prop) {
			this.prop = prop;
		}


		@Override
		public void acknowledge() {
			if (Domain.getProps().containsKey(prop.id)) {
				Prop propToSync = Domain.getProps().get(prop.id);
				propToSync.synchronize(prop);
			} else {
				Domain.getProps().put(prop.id, prop);
			}
		}


		@Override
		public int forClient() {
			return -1;
		}


		@Override
		public void prepare() {
		}
	}
}