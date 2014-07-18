package bloodandmithril.networking;

import java.util.LinkedList;
import java.util.List;

import bloodandmithril.core.Copyright;


/**
 * A response sent as a result of a {@link Request}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface Response {

	/** Acknowledges the response */
	public void acknowledge();

	/** If this returns anything other than -1, then only the client with the specified ID will {@link #acknowledge()} this {@link Response} */
	public int forClient();

	/** Prepares this responses before it is sent */
	public void prepare();

	public static class Responses {
		private LinkedList<Response> responses;
		private boolean executeInSingleThread;

		public Responses(boolean executeInSingleThread) {
			this.executeInSingleThread = executeInSingleThread;
			this.responses = new LinkedList<Response>();
		}

		public boolean executeInSingleThread() {
			return executeInSingleThread;
		}

		public List<Response> getResponses() {
			return responses;
		}

		public void add(Response response) {
			responses.add(response);
		}
	}
}