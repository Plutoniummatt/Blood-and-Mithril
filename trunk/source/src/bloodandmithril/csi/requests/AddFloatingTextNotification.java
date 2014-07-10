package bloodandmithril.csi.requests;

import bloodandmithril.csi.Response;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.UserInterface.FloatingText;

public class AddFloatingTextNotification implements Response {

	private final FloatingText floatingText;

	/**
	 * Constructor
	 */
	public AddFloatingTextNotification(FloatingText floatingText) {
		this.floatingText = floatingText;
	}


	@Override
	public void acknowledge() {
		UserInterface.addFloatingText(floatingText, true);
	}


	@Override
	public int forClient() {
		return -1;
	}


	@Override
	public void prepare() {
	}
}