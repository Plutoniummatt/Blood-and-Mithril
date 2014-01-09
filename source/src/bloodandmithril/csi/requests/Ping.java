package bloodandmithril.csi.requests;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;

/**
 * A simple Ping request
 *
 * @author Matt
 */
public class Ping implements Request {

	/** The ping */
	public final long ping;

	/**
	 * Constructor
	 */
	public Ping() {
		ping = System.currentTimeMillis();
	}


	@Override
	public Response respond() {
		return new Pong(ping);
	}


	/**
	 * The {@link Response} to {@link bloodandmithril.csi.requests.Ping}
	 *
	 * @author Matt
	 */
	public static class Pong implements Response {

		/** The ping */
		public long ping;

		/**
		 * Constructor
		 */
		public Pong(long ping) {
			this.ping = ping;
		}

		@Override
		public void acknowledge() {
			BloodAndMithrilClient.ping = System.currentTimeMillis() - ping;
		}
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