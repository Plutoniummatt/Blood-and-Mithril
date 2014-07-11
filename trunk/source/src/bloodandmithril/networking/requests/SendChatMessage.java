package bloodandmithril.networking.requests;

import static bloodandmithril.ui.UserInterface.FloatingText.floatingText;
import static com.google.common.collect.Iterables.tryFind;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.FloatingText;
import bloodandmithril.ui.components.window.ChatWindow;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

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

			if (!tryFind(UserInterface.layeredComponents, component -> {
				return component instanceof ChatWindow;
			}).isPresent()) {
				FloatingText floatingText = floatingText("New message!", Color.ORANGE, new Vector2(83, 50), true);
				floatingText.life = 2f;
				floatingText.maxLife = 2f;
				UserInterface.addFloatingText(floatingText, true);
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
		public Message(String sender, String message) {
			this.sender = sender;
			this.message = message;
		}
	}
}