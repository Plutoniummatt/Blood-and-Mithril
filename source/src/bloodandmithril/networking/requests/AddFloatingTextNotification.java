package bloodandmithril.networking.requests;

import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Response;
import bloodandmithril.ui.FloatingText;
import bloodandmithril.ui.UserInterface;

@Copyright("Matthew Peck 2014")
public class AddFloatingTextNotification implements Response {

	@Inject private transient UserInterface userInterface;

	private final FloatingText floatingText;
	private final int worldId;

	/**
	 * Constructor
	 */
	public AddFloatingTextNotification(final FloatingText floatingText, final int worldId) {
		this.floatingText = floatingText;
		this.worldId = worldId;
	}


	@Override
	public void acknowledge() {
		userInterface.addFloatingText(floatingText, worldId, true);
	}


	@Override
	public int forClient() {
		return -1;
	}


	@Override
	public void prepare() {
	}
}