package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;

/**
 * A simple Ping request
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
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
	public Responses respond() {
		Response response = new Pong(ping);
		Responses responses = new Response.Responses(false);
		responses.add(response);
		return responses;
	}


	/**
	 * The {@link Response} to {@link bloodandmithril.networking.requests.Ping}
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
			ClientServerInterface.ping = System.currentTimeMillis() - ping;
		}

		@Override
		public int forClient() {
			return -1;
		}

		@Override
		public void prepare() {
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