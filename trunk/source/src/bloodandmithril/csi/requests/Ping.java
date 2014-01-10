package bloodandmithril.csi.requests;

import java.util.List;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;

import com.google.common.collect.Lists;

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
	public List<Response> respond() {
		Response response = new Pong(ping);
		return Lists.newArrayList(response);
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