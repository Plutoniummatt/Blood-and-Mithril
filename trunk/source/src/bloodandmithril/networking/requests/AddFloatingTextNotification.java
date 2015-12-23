package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Response;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.FloatingText;

@Copyright("Matthew Peck 2014")
public class AddFloatingTextNotification implements Response {

	private final FloatingText floatingText;
	private final int worldId;

	/**
	 * Constructor
	 */
	public AddFloatingTextNotification(FloatingText floatingText, int worldId) {
		this.floatingText = floatingText;
		this.worldId = worldId;
	}


	@Override
	public void acknowledge() {
		UserInterface.addFloatingText(floatingText, worldId, true);
	}


	@Override
	public int forClient() {
		return -1;
	}


	@Override
	public void prepare() {
	}
}