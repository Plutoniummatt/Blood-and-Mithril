package bloodandmithril.csi.requests;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.ui.components.window.ChatWindow;

/**
 * {@link Request} sent when a client sends a chat message
 *
 * @author Matt
 */
public class SendChatMessage implements Request {

	private final Message message;

	/**
	 * Constructor
	 */
	public SendChatMessage(Message message) {
		this.message = message;
	}


	@Override
	public Responses respond() {
		Responses responses = new Responses(false);
		responses.add(new SendChatMessageResponse(message));
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


	public static class SendChatMessageResponse implements Response {

		private final Message message;

		/**
		 * Constructor
		 */
		public SendChatMessageResponse(Message message) {
			this.message = message;
		}

		@Override
		public void acknowledge() {
			ChatWindow.addMessage(message);
		}

		@Override
		public int forClient() {
			return -1;
		}

		@Override
		public void prepare() {
		}
	}


	public static class Message {
		public final String sender, message;

		/**
		 * Constructor
		 */
		public Message(String sender, String message) {
			this.sender = sender;
			this.message = message;
		}
	}
}