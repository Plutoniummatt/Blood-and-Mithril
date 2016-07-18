package bloodandmithril.networking.requests;

import static com.google.common.collect.Iterables.tryFind;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.ChatWindow;

/**
 * {@link Request} sent when a client sends a chat message
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class SendChatMessage implements Request {

	private final Message message;

	/**
	 * Constructor
	 */
	public SendChatMessage(final Message message) {
		this.message = message;
	}


	@Override
	public Responses respond() {
		final Responses responses = new Responses(false);
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
		public SendChatMessageResponse(final Message message) {
			this.message = message;
		}

		@Override
		public void acknowledge() {
			ChatWindow.addMessage(message);

			if (!tryFind(Wiring.injector().getInstance(UserInterface.class).getLayeredComponents(), component -> {
				return component instanceof ChatWindow;
			}).isPresent()) {
				UserInterface.addUIFloatingText("New Message!", Color.ORANGE, new Vector2(83, 50));
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


	public static class Message {
		public final String sender, message;

		/**
		 * Constructor
		 */
		public Message(final String sender, final String message) {
			this.sender = sender;
			this.message = message;
		}
	}
}