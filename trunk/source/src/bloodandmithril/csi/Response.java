package bloodandmithril.csi;

import java.util.LinkedList;


/**
 * A response sent as a result of a {@link Request}
 *
 * @author Matt
 */
public interface Response {

	/** Acknowledges the response */
	public void acknowledge();
	
	public static class Responses {
		public LinkedList<Response> responses;
		private boolean executeInSingleThread;
		
		public Responses(boolean executeInSingleThread, LinkedList<Response> responses) {
			this.executeInSingleThread = executeInSingleThread;
			this.responses = responses;
		}
		
		public boolean executeInSingleThread() {
			return executeInSingleThread;
		}
	}
}