package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
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

	/**
	 * 
	 */
	private static final long serialVersionUID = 3006451944759307548L;
	private final int propId;
	private final int worldId;

	public SynchronizePropRequest(final int propId, final int worldId) {
		this.propId = propId;
		this.worldId = worldId;
	}


	@Override
	public Responses respond() {
		final Responses responses = new Responses(false);
		responses.add(new SynchronizePropResponse(Domain.getWorld(worldId).props().getProp(propId)));
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

		/**
		 * 
		 */
		private static final long serialVersionUID = 1882634540580465303L;
		private final Prop prop;

		public SynchronizePropResponse(final Prop prop) {
			this.prop = prop;
		}


		@Override
		public void acknowledge() {
			if (Domain.getWorld(prop.getWorldId()).props().hasProp(prop.id)) {
				final Prop propToSync = Domain.getWorld(prop.getWorldId()).props().getProp(prop.id);
				propToSync.synchronizeProp(prop);
				if (propToSync instanceof Container) {
					((Container) propToSync).synchronizeContainer((Container) prop);
				}
				if (propToSync instanceof Construction) {
					((Construction) propToSync).synchronizeConstruction((Construction) prop);
				}
			} else {
				Domain.getWorld(prop.getWorldId()).props().addProp(prop);
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