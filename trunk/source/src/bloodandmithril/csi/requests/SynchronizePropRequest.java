package bloodandmithril.csi.requests;

import java.util.LinkedList;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.GameWorld;

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
		Responses responses = new Responses(false, new LinkedList<Response>());
		responses.responses.add(new SynchronizePropResponse(GameWorld.props.get(propId)));
		responses.responses.add(new TransferItems.RefreshWindowsResponse());
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
			if (GameWorld.props.containsKey(prop.id)) {
				Prop propToSync = GameWorld.props.get(prop.id);
				propToSync.synchronize(prop);
			} else {
				GameWorld.props.put(prop.id, prop);
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