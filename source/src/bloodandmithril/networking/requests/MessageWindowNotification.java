package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Response;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.SerializableFunction;

/**
 * A {@link Response} that notifies clients to display a message window
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class MessageWindowNotification implements Response {

	private int client;
	private String title, message;
	private SerializableFunction<Boolean> function;

	/**
	 * Constructor
	 * @param function
	 */
	public MessageWindowNotification(int client, String title, String message, SerializableFunction<Boolean> function) {
		this.client = client;
		this.title = title;
		this.message = message;
		this.function = function;
	}


	@Override
	public void acknowledge() {
		if (function.call()) {
			UserInterface.addMessage(title, message);
		}
	}


	@Override
	public int forClient() {
		return client;
	}


	@Override
	public void prepare() {
	}
}