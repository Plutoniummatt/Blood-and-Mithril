package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.requests.RefreshWindows.RefreshWindowsResponse;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} to synchronize a {@link Prop}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class SynchronizePropRequest implements Request {

	private final int propId;

	public SynchronizePropRequest(int propId) {
		this.propId = propId;
	}


	@Override
	public Responses respond() {
		Responses responses = new Responses(false);
		responses.add(new SynchronizePropResponse(Domain.getProps().get(propId)));
		responses.add(new RefreshWindowsResponse());
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
				propToSync.synchronizeProp(prop);
				if (propToSync instanceof Container) {
					((Container) propToSync).synchronizeContainer((Container) prop);
				}
				if (propToSync instanceof Construction) {
					((Construction) propToSync).synchronizeConstruction((Construction) prop);
				}
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