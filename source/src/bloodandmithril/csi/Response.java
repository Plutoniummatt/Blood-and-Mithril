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
	
	/** If this returns anything other than -1, then only the client with the specified ID will {@link #acknowledge()} this {@link Response} */
	public int forClient();
	
	/** Prepares this responses before it is sent */
	public void prepare();
	
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